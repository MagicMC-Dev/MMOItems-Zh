package net.Indyuce.mmoitems.manager;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.element.Element;
import io.lumine.mythic.lib.util.annotation.BackwardsCompatibility;
import io.lumine.mythic.lib.util.lang3.Validate;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.stat.annotation.DeprecatedStat;
import net.Indyuce.mmoitems.stat.annotation.HasCategory;
import net.Indyuce.mmoitems.stat.category.StatCategory;
import net.Indyuce.mmoitems.stat.type.*;
import net.Indyuce.mmoitems.util.ElementStatType;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;

public class StatManager {
    private final Map<String, ItemStat<?, ?>> stats = new LinkedHashMap<>();
    private final Map<String, StatCategory> categories = new HashMap<>();

    /**
     * If, for whatever reason, a stat needs to change its internal
     * string ID, this map keeps a reference for the deprecated old
     * IDs while being separated from the main ItemStat map.
     */
    @BackwardsCompatibility(version = "not_specified")
    private final Map<String, ItemStat<?, ?>> legacyAliases = new HashMap<>();

    /*
     * These lists are sets of stats collected when the stats are registered for
     * the first time to make their access easier. Check the classes
     * individually to understand better
     */
    private final List<DoubleStat> numericStats = new ArrayList<>();
    private final List<ItemRestriction> itemRestrictions = new ArrayList<>();
    private final List<ConsumableItemInteraction> consumableActions = new ArrayList<>();
    private final List<PlayerConsumable> playerConsumables = new ArrayList<>();

    /**
     * Load default stats using java reflection, get all public static final
     * fields in the ItemStat and register them as stat instances
     */
    public void loadBuiltins() {

        // Builtin categories
        for (Field field : StatCategory.class.getFields())
            try {
                if (Modifier.isStatic(field.getModifiers())
                        && Modifier.isFinal(field.getModifiers())
                        && field.get(null) instanceof StatCategory)
                    registerCategory((StatCategory) field.get(null));
            } catch (IllegalArgumentException | IllegalAccessException exception) {
                MMOItems.plugin.getLogger().log(Level.SEVERE, String.format("Couldn't register category called '%s': %s", field.getName(), exception.getMessage()));
            }

        // Load builtin stats
        for (Field field : ItemStats.class.getFields())
            try {
                if (Modifier.isStatic(field.getModifiers())
                        && Modifier.isFinal(field.getModifiers())
                        && field.get(null) instanceof ItemStat
                        && field.getAnnotation(DeprecatedStat.class) == null)
                    register((ItemStat<?, ?>) field.get(null));
            } catch (IllegalArgumentException | IllegalAccessException exception) {
                MMOItems.plugin.getLogger().log(Level.SEVERE, String.format("Couldn't register stat called '%s': %s", field.getName(), exception.getMessage()));
            }

        // Custom stats
        loadCustomStats();
    }

    // TODO refactor with stat categories
    public void reload(boolean cleanFirst) {

        // Clean fictive numeric stats before
        if (cleanFirst)
            numericStats.removeIf(stat -> stat instanceof FakeElementalStat); // temporary fix, this is for elements TODO improve

        // Register elemental stats
        loadElements();

        // Load stat translation objects (nothing to do with stats)
        final ConfigurationSection statOptions = new ConfigFile("/language", "stats").getConfig();
        for (ItemStat<?, ?> stat : getAll())
            try {
                @Nullable Object object = statOptions.get(stat.getPath());
                if (object == null) object = statOptions.get(stat.getLegacyTranslationPath());
                stat.loadConfiguration(statOptions, object != null ? object : "<TranslationNotFound:" + stat.getPath() + ">");
            } catch (RuntimeException exception) {
                MMOItems.plugin.getLogger().log(Level.SEVERE, "Could not load translation info for stat '" + stat.getId() + "': " + exception.getMessage());
            }
    }

    /**
     * Load custom stats
     */
    private void loadCustomStats() {
        ConfigManager.DefaultFile.CUSTOM_STATS.checkFile();
        ConfigFile config = new ConfigFile("custom-stats");
        ConfigurationSection section = config.getConfig().getConfigurationSection("custom-stats");
        Validate.notNull(section, "Custom stats section is null");
        section.getKeys(true).stream().filter(section::isConfigurationSection).map(section::getConfigurationSection).filter(Objects::nonNull).forEach(this::registerCustomStat);
    }

    /**
     * Register all MythicLib elements as stats
     * <p>
     * TODO refactor with stat categories
     */
    public void loadElements() {

        // TODO workaround, remove it with MI7
        numericStats.removeIf(stat -> stat instanceof FakeElementalStat); // temporary fix, this is for elements TODO improve

        for (ElementStatType type : ElementStatType.values())
            for (Element element : MythicLib.plugin.getElements().getAll())
                numericStats.add(new FakeElementalStat(element, type));
    }

    public void registerCategory(@NotNull StatCategory category) {
        categories.put(category.getId(), category);
    }

    @NotNull
    public StatCategory getCategory(@NotNull String id) {
        return Objects.requireNonNull(categories.get(id), "No stat category found with ID '" + id + "'");
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
        return numericStats;
    }

    /**
     * @return Collection of all stats which constitute an item restriction:
     *         required level, required class, soulbound..
     */
    @NotNull
    public List<ItemRestriction> getItemRestrictionStats() {
        return itemRestrictions;
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

        // Default registry
        ItemStat<?, ?> stat = stats.get(id);
        if (stat != null) return stat;

        // Numeric registry (see to-do)
        stat = numericStats.stream().filter(doubleStat -> doubleStat.getId().equals(id)).findFirst().orElse(null);
        if (stat != null) return stat;

        // Legacy liases
        stat = legacyAliases.get(id);
        if (stat != null) return stat;

        // Non existing stat
        return null;
    }

    public void unregisterIf(Predicate<ItemStat<?, ?>> filter) {
        stats.values().removeIf(filter);
        numericStats.removeIf(filter);
        itemRestrictions.removeIf(stat -> filter.test((ItemStat<?, ?>) stat));
        consumableActions.removeIf(stat -> filter.test((ItemStat<?, ?>) stat));
        playerConsumables.removeIf(stat -> filter.test((ItemStat<?, ?>) stat));
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

        // Register stat
        stats.compute(stat.getId(), (id, current) -> {
            Validate.isTrue(current == null, "A stat with ID '" + id + "' already exists");
            return stat;
        });

        // Register aliases (backwards compatibility)
        for (String alias : stat.getAliases()) legacyAliases.put(alias, stat);

        // Use-case specific registries
        if (stat instanceof DoubleStat && !(stat instanceof GemStoneStat) && stat.isCompatible(Type.GEM_STONE))
            numericStats.add((DoubleStat) stat);
        if (stat instanceof ItemRestriction) itemRestrictions.add((ItemRestriction) stat);
        if (stat instanceof ConsumableItemInteraction) consumableActions.add((ConsumableItemInteraction) stat);
        if (stat instanceof PlayerConsumable) playerConsumables.add((PlayerConsumable) stat);

        // Stat category
        HasCategory statCatAnnot = stat.getClass().getAnnotation(HasCategory.class);
        if (statCatAnnot != null) stat.setCategory(getCategory(UtilityMethods.enumName(statCatAnnot.cat())));

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
            ItemStat<?, ?> stat = statClass.getConstructor(String.class, Material.class, String.class, String[].class, String[].class, Material[].class).newInstance(statId, Material.PAPER, name, lore, new String[]{"!miscellaneous", "!block", "all"}, new Material[0]);
            register(stat);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException("Unable to create a custom stat of type " + type, e);
        }
    }

    /**
     * @see #register(ItemStat)
     * @deprecated Stat IDs are now stored in the stat instance directly.
     */
    @Deprecated
    @SuppressWarnings("unused")
    public void register(@Nullable String id, @NotNull ItemStat<?, ?> stat) {
        register(stat);
    }

    /**
     * @see #loadBuiltins()
     */
    @Deprecated
    public void loadInternalStats() {
        loadBuiltins();
    }
}
