package net.Indyuce.mmoitems.api.item.template;

import io.lumine.mythic.lib.util.PostLoadAction;
import io.lumine.mythic.lib.util.PreloadedObject;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class ModifierGroup extends ModifierNode implements PreloadedObject {
    private final int min, max;
    private final List<ModifierNode> children;

    private final PostLoadAction postLoadAction = new PostLoadAction(config -> {
        for (String key : config.getConfigurationSection("modifiers").getKeys(false))
            try {
                final ModifierNode child = ModifierNode.fromConfig(key, config.get("modifiers." + key));
                if (child instanceof PreloadedObject) ((PreloadedObject) child).getPostLoadAction().performAction();
                ModifierGroup.this.children.add(child);
            } catch (RuntimeException exception) {
                MMOItems.plugin.getLogger().log(Level.WARNING, "Could not load parent modifier node '" + key + "' of modifier group '" + getId() + "': " + exception.getMessage());
            }
    });

    public ModifierGroup(@NotNull String nodeId, @NotNull Object configObject) {
        super(nodeId, configObject);

        if (getReferenceNode() != null) {
            Validate.isTrue(getReferenceNode() instanceof ModifierGroup, "Reference node should be a modifier group");
            final ModifierGroup parent = (ModifierGroup) getReferenceNode();
            children = parent.children;
            final ConfigurationSection config = configObject instanceof ConfigurationSection ? (ConfigurationSection) configObject : null;
            min = config != null && config.contains("min") ? config.getInt("min") : parent.min;
            max = config != null && config.contains("max") ? config.getInt("max") : parent.max;
            return;
        }

        Validate.isTrue(configObject instanceof ConfigurationSection, "Must provide a config section when not using a reference node");
        final ConfigurationSection config = (ConfigurationSection) configObject;
        postLoadAction.cacheConfig(config);
        Validate.isTrue(config.contains("modifiers"), "You must provide a modifier list");

        min = config.getInt("min");
        max = config.getInt("max", config.getConfigurationSection("modifiers").getKeys(false).size());

        children = new ArrayList<>();
    }

    @Override
    public PostLoadAction getPostLoadAction() {
        return postLoadAction;
    }

    public List<ModifierNode> getChildren() {
        return children;
    }

    @Override
    public void whenCollected(@NotNull MMOItemBuilder builder, @NotNull UUID modifierId) {

        // Get deep working copy of children list
        final List<ModifierNode> children = new ArrayList<>();
        this.children.forEach(child -> children.add(child));
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
            if (random <= cumulatedWeights[i])
                return i;

        throw new IllegalArgumentException("Could not roll new modifier from group '" + getId() + "'");
    }
}
