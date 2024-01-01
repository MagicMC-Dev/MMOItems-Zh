package net.Indyuce.mmoitems.api.item.template;

import io.lumine.mythic.lib.util.PreloadedObject;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.UUID;

/**
 * An abstraction that groups both template modifier and modifier groups.
 * Groups and modifiers create a tree where simple modifiers are the leaves
 * and modifiers are nodes with arbitrarily many children.
 *
 * @author jules
 */
public abstract class ModifierNode implements PreloadedObject {
    private final String id;
    private final double chance, weight;

    @Nullable
    private final NameModifier nameModifier;

    /**
     * Should not be confused with the parent node. Instead of fully
     * defining a new modifier node, the user can reference another
     * public node, given that it has been registered publically
     * in the /modifiers folder.
     * <p>
     * The reference node points to that public node.
     */
    private final ModifierNode referenceNode;

    protected static final Random RANDOM = new Random();

    public ModifierNode(@NotNull String nodeId, @NotNull Object configObject) {
        this.id = nodeId;

        // Number -> simple reference
        if (configObject instanceof Number) {
            chance = 1;
            weight = ((Number) configObject).doubleValue();
            referenceNode = findReferenceNode(id);
            nameModifier = referenceNode.nameModifier;
        }

        // Path -> simple reference
        else if (configObject instanceof String) {
            chance = 1;
            weight = 0;
            referenceNode = findReferenceNode(configObject.toString());
            nameModifier = referenceNode.nameModifier;
        }

        // Load from configuration section
        else if (configObject instanceof ConfigurationSection) {
            final ConfigurationSection config = (ConfigurationSection) configObject;
            ModifierNode referenceNode = null;
            try {
                referenceNode = findReferenceNode(id);
            } catch (Throwable ignored) {
                // Do nothing
            }
            this.referenceNode = referenceNode;

            this.chance = config.getDouble("chance", referenceNode != null ? referenceNode.getChance() : 1);
            this.weight = config.getDouble("weight", referenceNode != null ? referenceNode.getWeight() : 0);

            this.nameModifier = config.contains("suffix") ? new NameModifier(NameModifier.ModifierType.SUFFIX, config.get("suffix"))
                    : config.contains("prefix") ? new NameModifier(NameModifier.ModifierType.PREFIX, config.get("prefix"))
                    : referenceNode != null ? referenceNode.nameModifier : null;

            Validate.isTrue(chance > 0, "Chance must be strictly positive");
        }

        // No type found
        else
            throw new IllegalArgumentException("Must be either a string or config section");
    }

    @NotNull
    private ModifierNode findReferenceNode(@NotNull String id) {
        final ModifierNode node = MMOItems.plugin.getTemplates().getModifierNode(id);
        Validate.notNull(node, "Could not find public modifier with ID '" + id + "'");
        return node;
    }

    @NotNull
    public String getId() {
        return id;
    }

    public double getWeight() {
        return weight;
    }

    public double getChance() {
        return chance;
    }

    @Nullable
    public ModifierNode getReferenceNode() {
        return referenceNode;
    }

    @Nullable
    public NameModifier getNameModifier() {
        return nameModifier;
    }

    public boolean hasNameModifier() {
        return nameModifier != null;
    }

    public boolean rollChance() {
        return RANDOM.nextDouble() < chance;
    }

    public boolean collect(@NotNull MMOItemBuilder builder) {

        // Roll modifier node chance
        if (!rollChance()) return false;

        // Check modifier node weight constraint
        if (weight > builder.getCapacity()) return false;

        // Modifier UUID
        final UUID modifierId = UUID.randomUUID();
        builder.reduceCapacity(weight);
        if (hasNameModifier()) builder.addModifier(getNameModifier(), modifierId);
        whenCollected(builder, modifierId);
        return true;
    }

    /**
     * This should apply the stats of the modifier nodes
     * and call subsequent modifier nodes if necessary.
     */
    public abstract void whenCollected(@NotNull MMOItemBuilder builder, @NotNull UUID modifierId);

    @NotNull
    public static ModifierNode fromConfig(@NotNull String nodeId, @NotNull Object configObject) {
        if (configObject instanceof ConfigurationSection) {
            final ConfigurationSection config = (ConfigurationSection) configObject;
            if (config.contains("modifiers")) return new ModifierGroup(nodeId, configObject);
            if (config.contains("stats")) return new TemplateModifier(nodeId, configObject);
            return fromReference(nodeId, configObject);
        }

        if (configObject instanceof Number) return fromReference(nodeId, configObject);

        if (configObject instanceof String)
            return MMOItems.plugin.getTemplates().getModifierNode(configObject.toString());

        throw new IllegalArgumentException("You must provide either a string, config section or number");
    }

    @NotNull
    private static ModifierNode fromReference(@NotNull String nodeId, @NotNull Object configObject) {
        final ModifierNode ref = MMOItems.plugin.getTemplates().getModifierNode(nodeId);
        Validate.notNull(ref, "Could not find reference node called '" + nodeId + "'");
        if (ref instanceof TemplateModifier) return new TemplateModifier(nodeId, configObject);
        if (ref instanceof ModifierGroup) return new ModifierGroup(nodeId, configObject);
        throw new IllegalArgumentException("Could not match modifier node");
    }
}
