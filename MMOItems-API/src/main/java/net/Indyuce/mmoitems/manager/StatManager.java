package net.Indyuce.mmoitems.manager;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.element.Element;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.stat.type.*;
import net.Indyuce.mmoitems.util.ElementStatType;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.logging.Level;

public class StatManager {
    private final Map<String, ItemStat<?, ?>> stats = new LinkedHashMap<>();

    /*
     * These lists are sets of stats collected when the stats are registered for
     * the first time to make their access easier. Check the classes
     * individually to understand better
     */
    private final List<DoubleStat> numeric = new ArrayList<>();
    private final List<ItemRestriction> itemRestriction = new ArrayList<>();
    private final List<ConsumableItemInteraction> consumableActions = new ArrayList<>();
    private final List<PlayerConsumable> playerConsumables = new ArrayList<>();

    /**
     * Load default stats using java reflection, get all public static final
     * fields in the ItemStat and register them as stat instances
     */
    public void load() {
        for (Field field : ItemStats.class.getFields())
            try {
                if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers()) && field.get(null) instanceof ItemStat)
                    register((ItemStat<?, ?>) field.get(null));
            } catch (IllegalArgumentException | IllegalAccessException exception) {
                MMOItems.plugin.getLogger().log(Level.WARNING, String.format("Couldn't register stat called '%s'", field.getName()), exception.getMessage());
            }

        // Custom stats
        loadCustom();
    }

    /**
     * @see FictiveNumericStat
     * @deprecated Needs refactor
     */
    @Deprecated
    public void reload(boolean cleanFirst) {

        // Clean fictive numeric stats before
        if (cleanFirst) numeric.removeIf(stat -> stat instanceof FictiveNumericStat);

        // Register elemental stats
        loadElements();

        // Register custom stats
        loadCustom();
    }

    /**
     * Load custom stats
     *
     * @deprecated Needs refactor
     */
    @Deprecated
    public void loadCustom() {
        ConfigManager.DefaultFile.CUSTOM_STATS.checkFile();
        ConfigFile config = new ConfigFile("custom-stats");
        ConfigurationSection section = config.getConfig().getConfigurationSection("custom-stats");
        Validate.notNull(section, "Custom stats section is null");
        section.getKeys(true).stream().filter(section::isConfigurationSection).map(section::getConfigurationSection).filter(Objects::nonNull).forEach(this::registerCustomStat);
    }

    /**
     * Register all MythicLib elements as stats
     *
     * @deprecated Needs refactor
     */
    @Deprecated
    public void loadElements() {
        for (ElementStatType type : ElementStatType.values())
            for (Element element : MythicLib.plugin.getElements().getAll())
                numeric.add(new FictiveNumericStat(element, type));
    }

    @NotNull
    public Collection<ItemStat<?, ?>> getAll() {
        return stats.values();
    }

    /**
     * @return Collection of all numeric stats like atk damage, crit strike
     *         chance, max mana... which can be applied on a gem stone. This is
     *         used when applying gem stones to quickly access all the stats
     *         which needs to be applied
     */
    @NotNull
    public List<DoubleStat> getNumericStats() {
        return numeric;
    }

    /**
     * @return Collection of all stats which constitute an item restriction:
     *         required level, required class, soulbound..
     */
    @NotNull
    public List<ItemRestriction> getItemRestrictionStats() {
        return itemRestriction;
    }

    /**
     * @return Collection of all stats implementing a consumable action like
     *         deconstructing, identifying...
     */
    @NotNull
    public List<ConsumableItemInteraction> getConsumableActions() {
        return consumableActions;
    }

    /**
     * @return Collection of all stats implementing self consumable like
     *         restore health, mana, hunger...
     */
    @NotNull
    public List<PlayerConsumable> getPlayerConsumables() {
        return playerConsumables;
    }

    public boolean has(String id) {
        return stats.containsKey(id);
    }

    @Nullable
    public ItemStat<?, ?> get(String id) {
        ItemStat<?, ?> stat = stats.getOrDefault(id, null);
        if (stat == null) {
            stat = numeric.stream().filter(doubleStat -> doubleStat.getId().equals(id)).findFirst().orElse(null);
        }
        return stat;
    }

    /**
     * @deprecated Stat IDs are now stored in the stat instance directly.
     *         Please use StatManager#register(ItemStat) instead
     */
    @Deprecated
    @SuppressWarnings("unused")
    public void register(String id, ItemStat<?, ?> stat) {
        register(stat);
    }

    /**
     * Registers a stat in MMOItems. It must be done right after MMOItems loads
     * before any manager is initialized because stats are commonly used when
     * loading configs.
     *
     * @param stat The stat to register
     */
    public void register(@NotNull ItemStat<?, ?> stat) {

        // Skip disabled stats.
        if (!stat.isEnabled()) return;

        // Safe check, this can happen with numerous extra RPG plugins
        if (stats.containsKey(stat.getId())) {
            MMOItems.plugin.getLogger().log(Level.WARNING, "Could not register stat '" + stat.getId() + "' as a stat with the same ID already exists.");
            return;
        }

        stats.put(stat.getId(), stat);

        // Custom registries
        if (stat instanceof DoubleStat && !(stat instanceof GemStoneStat) && stat.isCompatible(Type.GEM_STONE))
            numeric.add((DoubleStat) stat);
        if (stat instanceof ItemRestriction) itemRestriction.add((ItemRestriction) stat);
        if (stat instanceof ConsumableItemInteraction) consumableActions.add((ConsumableItemInteraction) stat);
        if (stat instanceof PlayerConsumable) playerConsumables.add((PlayerConsumable) stat);

        /*
         * Cache stat for every type which may have this stat. Really important
         * otherwise the stat will NOT be used anywhere in the plugin. This
         * process is also done in the TypeManager when registering new types
         * but since stats can be registered after types are loaded, we must
         * take it into account
         */
        if (MMOItems.plugin.getTypes() != null)
            MMOItems.plugin.getTypes().getAll().stream().filter(stat::isCompatible).forEach(type -> type.getAvailableStats().add(stat));
    }

    private void registerCustomStat(@NotNull ConfigurationSection section) {
        final String name = section.getString("name");
        final String type = section.getString("type");

        Validate.notNull(section, "Cannot register a custom stat from a null section");
        Validate.notNull(name, "Cannot register a custom stat without a name");
        Validate.notNull(type, "Cannot register a custom stat without a type");

        Class<? extends ItemStat<?, ?>> statClass;
        switch (type.toLowerCase()) {
            case "double":
                statClass = DoubleStat.class;
                break;
            case "boolean":
                statClass = BooleanStat.class;
                break;
            case "text":
                statClass = StringStat.class;
                break;
            case "text-list":
                statClass = StringListStat.class;
                break;
            default:
                throw new RuntimeException("Cannot register a custom stat of type " + type);
        }

        final String statId = String.format("custom_%s", name.replace(" ", "_")).toUpperCase();

        // Lore
        String[] lore = new String[0];
        if (section.isList("lore")) lore = section.getStringList("lore").toArray(new String[]{});
        else if (section.isString("lore")) lore = new String[]{section.getString("lore")};

        // Create a new stat instance
        try {
            ItemStat<?, ?> stat = statClass.getConstructor(String.class, Material.class, String.class, String[].class, String[].class, Material[].class)
                    .newInstance(statId, Material.PAPER, name, lore, new String[]{"!miscellaneous", "!block", "all"}, new Material[0]);
            register(stat);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException("Unable to create a custom stat of type " + type, e);
        }
    }
}
