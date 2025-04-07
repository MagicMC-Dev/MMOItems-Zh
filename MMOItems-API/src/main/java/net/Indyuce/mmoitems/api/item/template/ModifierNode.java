package net.Indyuce.mmoitems.api.item.template;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.util.PostLoadAction;
import io.lumine.mythic.lib.util.PreloadedObject;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;

/**
 * An abstraction that groups both template modifiers and modifier groups.
 * Modifiers apply stats and groups apply multiple modifiers at the same time.
 * Nodes can both apply stats and modifiers, like the nodes of a modifier tree.
 *
 * @author Jules
 */
public class ModifierNode implements PreloadedObject {
    private final String id;
    private final double chance, weight;
    private final int min, max;
    @Nullable
    private final NameModifier nameModifier;

    @Nullable
    private final List<ModifierNode> children;
    @Nullable
    private final Map<ItemStat, RandomStatData> data;

    private static final Random RANDOM = new Random();

    /**
     * Should not be confused with the parent node. Instead of fully
     * defining a new modifier node, the user can reference another
     * public node, given that it has been registered publicly in
     * the /modifiers folder.
     * <p>
     * The reference node points to that public node.
     */
    private final ModifierNode referenceNode;

    private final PostLoadAction postLoadAction = new PostLoadAction(config -> {

        // Post-load further references
        final ConfigurationSection modSection = config.getConfigurationSection("modifiers");
        if (modSection != null) for (String key : modSection.getKeys(false))
            try {
                final ModifierNode child = ModifierNode.fromConfig(key, config.get("modifiers." + key));
                child.getPostLoadAction().performAction();
                ModifierNode.this.children.add(child);
            } catch (RuntimeException exception) {
                MMOItems.plugin.getLogger().log(Level.SEVERE, "Could not load parent modifier node '" + key + "' of modifier group '" + getId() + "': " + exception.getMessage());
            }

        // Post-load stat data
        Validate.notNull(ModifierNode.this.data, "Internal error");
        final ConfigurationSection statSection = config.getConfigurationSection("stats");
        if (statSection != null) for (String key : statSection.getKeys(false))
            try {
                final String statId = UtilityMethods.enumName(key);
                final ItemStat<?, ?> stat = MMOItems.plugin.getStats().get(statId);
                Validate.notNull(stat, "Could not find stat with ID '" + statId + "'");
                ModifierNode.this.data.put(stat, stat.whenInitialized(statSection.get(key)));
            } catch (IllegalArgumentException exception) {
                MMOItems.plugin.getLogger().log(Level.SEVERE, "An error occurred while trying to load modifier node " + getId() + ": " + exception.getMessage());
            }
    });

    public ModifierNode(@NotNull String nodeId, @NotNull Object configObject) {
        this.id = nodeId;

        // Number -> simple reference
        if (configObject instanceof Number) {
            referenceNode = findReferenceNode(id);
            chance = referenceNode.chance;
            weight = ((Number) configObject).doubleValue();
            nameModifier = referenceNode.nameModifier;
        }

        // Path -> simple reference
        else if (configObject instanceof String) {
            referenceNode = findReferenceNode(configObject.toString());
            chance = referenceNode.chance;
            weight = referenceNode.weight;
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

            this.nameModifier = config.contains("suffix") ? new NameModifier(NameModifier.ModifierType.SUFFIX, config.get("suffix")) : config.contains("prefix") ? new NameModifier(NameModifier.ModifierType.PREFIX, config.get("prefix")) : referenceNode != null ? referenceNode.nameModifier : null;

            Validate.isTrue(chance > 0, "Chance must be strictly positive");
        }

        // No type found
        else throw new IllegalArgumentException("Must be either a string, number config section");

        if (referenceNode != null) {

            // Group
            children = referenceNode.children;
            final ConfigurationSection config = configObject instanceof ConfigurationSection ? (ConfigurationSection) configObject : null;
            min = config != null && config.contains("min") ? config.getInt("min") : referenceNode.min;
            max = config != null && config.contains("max") ? config.getInt("max") : referenceNode.max;

            // Modifier
            data = referenceNode.data;
        }

        // No reference node
        else {
            Validate.isTrue(configObject instanceof ConfigurationSection, "Must provide a config section when not using a reference node");
            final ConfigurationSection config = (ConfigurationSection) configObject;

            // Group
            min = config.getInt("min");
            max = config.getInt("max", -1);
            children = new ArrayList<>();
            postLoadAction.cacheConfig(config);

            // Modifier
            this.data = new HashMap<>();
        }
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

    @NotNull
    public Map<ItemStat, RandomStatData> getItemData() {
        return data;
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

    @Override
    public PostLoadAction getPostLoadAction() {
        return postLoadAction;
    }

    public List<ModifierNode> getChildren() {
        return children;
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

    public void whenCollected(@NotNull MMOItemBuilder builder, @NotNull UUID modifierId) {

        // VANILLA MODIFIER SECTION

        data.forEach((itemStat, statData) -> builder.addModifierData(itemStat, statData.randomize(builder), modifierId));

        // MODIFIER GROUP SECTION

        // Get deep working copy of children list
        final List<ModifierNode> children = new ArrayList<>(this.children);
        if (builder.getTemplate().hasOption(MMOItemTemplate.TemplateOption.ROLL_MODIFIER_CHECK_ORDER))
            Collections.shuffle(children);

        final int effectiveMax = max <= 0 ? children.size() : Math.min(max, children.size());

        // Normally roll all modifiers until max amount is reached
        int i = 0;
        while (this.children.size() - children.size() < effectiveMax && i < children.size()) {
            final ModifierNode next = children.get(i);
            if (next.collect(builder)) children.remove(i);
            else i++;
        }

        // If needed, select more using chances as probability distributions
        while (this.children.size() - children.size() < min) {
            final ModifierNode node = children.remove(rollModifier(children));
            node.collect(builder);
        }
    }

    @NotNull
    public static ModifierNode fromConfig(@NotNull String nodeId, @NotNull Object configObject) {
        if (configObject instanceof Number || configObject instanceof String)
            Validate.notNull(findReferenceNode(nodeId), "Could not find reference node called '" + nodeId + "'");
        return new ModifierNode(nodeId, configObject);
    }

    @NotNull
    private static ModifierNode findReferenceNode(@NotNull String id) {
        final ModifierNode node = MMOItems.plugin.getTemplates().getModifierNode(id);
        Validate.notNull(node, "Could not find public modifier with ID '" + id + "'");
        return node;
    }

    private int rollModifier(@NotNull List<ModifierNode> children) {

        // Calculate cumulated weights and total weight
        final double[] cumulatedWeights = new double[children.size()];
        double totalWeight = 0;
        for (int i = 0; i < children.size(); i++) {
            totalWeight += children.get(i).getChance();
            cumulatedWeights[i] = totalWeight;
        }

        final double random = RANDOM.nextDouble() * totalWeight;
        for (int i = 0; i < children.size(); i++)
            if (random <= cumulatedWeights[i]) return i;

        throw new IllegalArgumentException("Could not roll new modifier from group '" + getId() + "'");
    }
}
