package net.Indyuce.mmoitems.api.item.template;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.util.PostLoadAction;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class TemplateModifier extends ModifierNode {
    private final Map<ItemStat, RandomStatData> data;

    private final PostLoadAction postLoadAction = new PostLoadAction(config -> {
        Validate.notNull(config.getConfigurationSection("stats"), "Could not find base item data");
        for (String key : config.getConfigurationSection("stats").getKeys(false))
            try {
                final String statId = UtilityMethods.enumName(key);
                final ItemStat stat = MMOItems.plugin.getStats().get(statId);
                Validate.notNull(stat, "Could not find stat with ID '" + statId + "'");
                TemplateModifier.this.data.put(stat, stat.whenInitialized(config.get("stats." + key)));
            } catch (IllegalArgumentException exception) {
                MMOItems.plugin.getLogger().log(Level.INFO, "An error occurred while trying to load modifier node " + getId() + ": " + exception.getMessage());
            }
    });

    public TemplateModifier(@NotNull ConfigurationSection config) {
        this(config.getName(), config);
    }

    /**
     * Loads an item gen modifier from a configuration section. If you provide
     * the ItemGenManager, you will be able to use the 'parent' option to
     * redirect that modifier to a public gen modifier.
     *
     * @param nodeId       Internal ID of the modifier
     * @param configObject Either a string or a config section containing the template data
     */
    public TemplateModifier(@NotNull String nodeId, @NotNull Object configObject) {
        super(nodeId, configObject);

        // Use reference node
        if (getReferenceNode() != null) {
            Validate.isTrue(getReferenceNode() instanceof TemplateModifier, "Reference node should be a simple modifier");
            final TemplateModifier parent = (TemplateModifier) getReferenceNode();
            data = parent.data;
            return;
        }

        // Make sure it's a config section
        Validate.isTrue(configObject instanceof ConfigurationSection, "Must provide a config section when not using a reference node");
        postLoadAction.cacheConfig((ConfigurationSection) configObject);
        this.data = new HashMap<>();
    }

    @Override
    public PostLoadAction getPostLoadAction() {
        return postLoadAction;
    }

    @NotNull
    public Map<ItemStat, RandomStatData> getItemData() {
        return data;
    }

    @Override
    public void whenCollected(@NotNull MMOItemBuilder builder, @NotNull UUID modifierId) {
        data.forEach((itemStat, statData) -> builder.addModifierData(itemStat, statData.randomize(builder), modifierId));
    }
}
