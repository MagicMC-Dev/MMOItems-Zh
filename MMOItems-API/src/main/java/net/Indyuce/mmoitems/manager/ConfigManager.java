package net.Indyuce.mmoitems.manager;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.ReforgeOptions;
import net.Indyuce.mmoitems.api.item.util.ConfigItem;
import net.Indyuce.mmoitems.api.item.util.ConfigItems;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.skill.RegisteredSkill;
import net.Indyuce.mmoitems.stat.GemUpgradeScaling;
import net.Indyuce.mmoitems.stat.LuteAttackEffectStat.LuteAttackEffect;
import net.Indyuce.mmoitems.stat.StaffSpiritStat.StaffSpirit;
import net.Indyuce.mmoitems.util.MMOUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

public class ConfigManager implements Reloadable {

    // cached config files
    private ConfigFile abilities, loreFormat, messages, potionEffects, stats, attackEffects, dynLore;

    // Cached config options
    public boolean replaceMushroomDrops, worldGenEnabled, upgradeRequirementsCheck, keepSoulboundOnDeath, rerollOnItemUpdate, opStatsEnabled;
    public String abilitySplitter;
    public double soulboundBaseDamage, soulboundPerLvlDamage, levelSpread;
    public NumericStatFormula defaultItemCapacity;
    public ReforgeOptions revisionOptions, phatLootsOptions;
    public final List<String> opStats = new ArrayList<>();

    private static final String[] fileNames = {"abilities", "messages", "potion-effects", "stats", "items", "attack-effects"};
    private static final String[] languages = {"french", "chinese", "spanish", "russian", "polish"};

    public ConfigManager() {

        mkdir("layouts");
        mkdir("item");
        mkdir("language");
        mkdir("language/lore-formats");
        mkdir("modifiers");

        File craftingStationsFolder = new File(MMOItems.plugin.getDataFolder() + "/crafting-stations");
        if (!craftingStationsFolder.exists()) {
            if (craftingStationsFolder.mkdir()) {
                try {
                    JarFile jarFile = new JarFile(MMOItems.plugin.getJarFile());
                    for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements(); ) {
                        String name = entries.nextElement().getName();
                        if (name.startsWith("default/crafting-stations/") && name.length() > "default/crafting-stations/".length())
                            Files.copy(MMOItems.plugin.getResource(name),
                                    new File(MMOItems.plugin.getDataFolder() + "/crafting-stations", name.split("/")[2]).toPath());
                    }
                    jarFile.close();
                } catch (IOException exception) {
                    MMOItems.plugin.getLogger().log(Level.WARNING, "Could not load default crafting stations.");
                }
            } else MMOItems.plugin.getLogger().log(Level.WARNING, "Could not create directory!");
        }

        // Setup non existing language files
        for (String language : languages) {
            File languageFolder = new File(MMOItems.plugin.getDataFolder() + "/language/" + language);
            if (!languageFolder.exists())
                if (languageFolder.mkdir()) {
                    for (String fileName : fileNames)
                        if (!new File(MMOItems.plugin.getDataFolder() + "/language/" + language, fileName + ".yml").exists()) {
                            try {
                                Files.copy(MMOItems.plugin.getResource("language/" + language + "/" + fileName + ".yml"),
                                        new File(MMOItems.plugin.getDataFolder() + "/language/" + language, fileName + ".yml").getAbsoluteFile().toPath(),
                                        StandardCopyOption.REPLACE_EXISTING);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                } else MMOItems.plugin.getLogger().log(Level.WARNING, "Could not load default crafting stations.");
        }

        // Load files with default configuration
        for (DefaultFile file : DefaultFile.values())
            if (file.isAutomatic())
                file.checkFile();

        /*
         * Setup /item files after generating the default /item files otherwise
         * they will be empty!
         */
        MMOItems.plugin.getTypes().getAll().forEach(type -> type.getConfigFile().setup());

        ConfigFile items = new ConfigFile("/language", "items");
        for (ConfigItem item : ConfigItems.values) {
            if (!items.getConfig().contains(item.getId())) {
                items.getConfig().createSection(item.getId());
                item.setup(items.getConfig().getConfigurationSection(item.getId()));
            }
            item.update(items.getConfig());
        }
        items.save();

        ConfigFile messages = new ConfigFile("/language", "messages");
        for (Message message : Message.values()) {
            String path = message.name().toLowerCase().replace("_", "-");
            if (!messages.getConfig().contains(path))
                messages.getConfig().set(path, message.getDefault());
        }
        messages.save();

        ConfigFile abilities = new ConfigFile("/language", "abilities");
        for (RegisteredSkill ability : MMOItems.plugin.getSkills().getAll()) {
            String path = ability.getHandler().getLowerCaseId();
            if (!abilities.getConfig().getKeys(true).contains("ability." + path))
                abilities.getConfig().set("ability." + path, ability.getName());

            for (String modifier : ability.getHandler().getModifiers())
                if (!abilities.getConfig().getKeys(true).contains("modifier." + modifier))
                    abilities.getConfig().set("modifier." + modifier, MMOUtils.caseOnWords(modifier.replace("-", " ")));
        }
        for (TriggerType mode : TriggerType.values())
            if (!abilities.getConfig().contains("cast-mode." + mode.getLowerCaseId()))
                abilities.getConfig().set("cast-mode." + mode.getLowerCaseId(), mode.getName());
        abilities.save();

        ConfigFile potionEffects = new ConfigFile("/language", "potion-effects");
        for (PotionEffectType effect : PotionEffectType.values()) {
            if (effect == null)
                continue;

            String path = effect.getName().toLowerCase().replace("_", "-");
            if (!potionEffects.getConfig().contains(path))
                potionEffects.getConfig().set(path, MMOUtils.caseOnWords(effect.getName().toLowerCase().replace("_", " ")));
        }

        potionEffects.save();

        ConfigFile attackEffects = new ConfigFile("/language", "attack-effects");
        for (StaffSpirit spirit : StaffSpirit.values()) {
            String path = spirit.name().toLowerCase().replace("_", "-");
            if (!attackEffects.getConfig().contains("staff-spirit." + path))
                attackEffects.getConfig().set("staff-spirit." + path, "&7" + AltChar.listSquare + " " + spirit.getDefaultName());
        }

        for (LuteAttackEffect effect : LuteAttackEffect.values()) {
            String path = effect.name().toLowerCase().replace("_", "-");
            if (!attackEffects.getConfig().contains("lute-attack." + path))
                attackEffects.getConfig().set("lute-attack." + path, "&7" + AltChar.listSquare + " " + effect.getDefaultName() + " Attacks");
        }
        attackEffects.save();

        /*
         * Only load config files after they have been initialized (above) so
         * they do not crash the first time they generate and so we do not have
         * to restart the server
         */
        reload();
    }

    public void reload() {
        MMOItems.plugin.reloadConfig();

        abilities = new ConfigFile("/language", "abilities");
        loreFormat = new ConfigFile("/language", "lore-format");
        messages = new ConfigFile("/language", "messages");
        potionEffects = new ConfigFile("/language", "potion-effects");
        stats = new ConfigFile("/language", "stats");
        attackEffects = new ConfigFile("/language", "attack-effects");
        dynLore = new ConfigFile("/language", "dynamic-lore");

        /*
         * Reload cached config options for quicker access - these options are
         * used in runnables, it is thus better to cache them
         */
        replaceMushroomDrops = MMOItems.plugin.getConfig().getBoolean("custom-blocks.replace-mushroom-drops");
        worldGenEnabled = MMOItems.plugin.getConfig().getBoolean("custom-blocks.enable-world-gen");

        abilitySplitter = getStatFormat("ability-splitter");
        soulboundBaseDamage = MMOItems.plugin.getConfig().getDouble("soulbound.damage.base");
        soulboundPerLvlDamage = MMOItems.plugin.getConfig().getDouble("soulbound.damage.per-lvl");
        upgradeRequirementsCheck = MMOItems.plugin.getConfig().getBoolean("item-upgrade-requirements-check");
        GemUpgradeScaling.defaultValue = MMOItems.plugin.getConfig().getString("gem-upgrade-default", GemUpgradeScaling.SUBSEQUENT);
        keepSoulboundOnDeath = MMOItems.plugin.getConfig().getBoolean("soulbound.keep-on-death");
        rerollOnItemUpdate = MMOItems.plugin.getConfig().getBoolean("item-revision.reroll-when-updated");
        levelSpread = MMOItems.plugin.getConfig().getDouble("item-level-spread");

        opStatsEnabled = MMOItems.plugin.getConfig().getBoolean("op-item-stats.enabled");
        opStats.clear();
        for (String key : MMOItems.plugin.getConfig().getStringList("op-item-stats.stats"))
            opStats.add(UtilityMethods.enumName(key));

        ConfigurationSection keepData = MMOItems.plugin.getConfig().getConfigurationSection("item-revision.keep-data");
        ConfigurationSection phatLoots = MMOItems.plugin.getConfig().getConfigurationSection("item-revision.phat-loots");
        ReforgeOptions.dropRestoredGems = MMOItems.plugin.getConfig().getBoolean("item-revision.drop-extra-gems", true);
        revisionOptions = keepData != null ? new ReforgeOptions(keepData) : new ReforgeOptions(false, false, false, false, false, false, false, true);
        phatLootsOptions = phatLoots != null ? new ReforgeOptions(phatLoots) : new ReforgeOptions(false, false, false, false, false, false, false, true);

        List<String> exemptedPhatLoots = MMOItems.plugin.getConfig().getStringList("item-revision.disable-phat-loot");
        for (String epl : exemptedPhatLoots)
            phatLootsOptions.addToBlacklist(epl);

        try {
            defaultItemCapacity = new NumericStatFormula(MMOItems.plugin.getConfig().getConfigurationSection("default-item-capacity"));
        } catch (IllegalArgumentException exception) {
            defaultItemCapacity = new NumericStatFormula(5, .05, .1, .3);
            MMOItems.plugin.getLogger().log(Level.INFO,
                    "An error occurred while trying to load default capacity formula for the item generator, using default: "
                            + exception.getMessage());
        }

        ConfigFile items = new ConfigFile("/language", "items");
        for (ConfigItem item : ConfigItems.values)
            item.update(items.getConfig().getConfigurationSection(item.getId()));
    }

    /**
     * @return Can this block material be broken by tool mechanics
     *         like 'Bouncing Crack'
     */
    public boolean isBlacklisted(Material material) {
        return MMOItems.plugin.getConfig().getStringList("block-blacklist").contains(material.name());
    }

    @NotNull
    public String getStatFormat(String path) {
        String found = stats.getConfig().getString(path);
        return found == null ? "<TranslationNotFound:" + path + ">" : found;
    }

    @NotNull
    public String getMessage(String path) {
        String found = messages.getConfig().getString(path);
        return MythicLib.plugin.parseColors(found == null ? "<MessageNotFound:" + path + ">" : found);
    }

    @NotNull
    public String getCastingModeName(@NotNull TriggerType mode) {
        return abilities.getConfig().getString("cast-mode." + mode.getLowerCaseId(), mode.name());
    }



    @Deprecated
    public String getModifierName(String path) {
        return abilities.getConfig().getString("modifier." + path);
    }

    public List<String> getDefaultLoreFormat() {
        return loreFormat.getConfig().getStringList("lore-format");
    }

    public String getPotionEffectName(PotionEffectType type) {
        return potionEffects.getConfig().getString(type.getName().toLowerCase().replace("_", "-"));
    }

    public String getLuteAttackEffectName(LuteAttackEffect effect) {
        return attackEffects.getConfig().getString("lute-attack." + effect.name().toLowerCase().replace("_", "-"));
    }

    public String getStaffSpiritName(StaffSpirit spirit) {
        return attackEffects.getConfig().getString("staff-spirit." + spirit.name().toLowerCase().replace("_", "-"));
    }

    /**
     * @deprecated See {@link net.Indyuce.mmoitems.api.item.util.LoreUpdate}
     */
    @Deprecated
    public String getDynLoreFormat(String input) {
        return dynLore.getConfig().getString("format." + input);
    }

    /**
     * Creates an empty directory in the MMOItems plugin folder if it does not
     * exist
     *
     * @param path The path of your folder
     */
    private void mkdir(String path) {
        File folder = new File(MMOItems.plugin.getDataFolder() + "/" + path);
        if (!folder.exists())
            if (!folder.mkdir())
                MMOItems.plugin.getLogger().log(Level.WARNING, "Could not create directory!");
    }

    /*
     * All config files that have a default configuration are stored here, they
     * get copied into the plugin folder when the plugin enables
     */
    public enum DefaultFile {

        // Default general config files -> /MMOItems
        ITEM_TIERS("", "item-tiers"),
        ITEM_TYPES("", "item-types", true),
        DROPS("", "drops"),
        ITEM_SETS("", "item-sets"),
        GEN_TEMPLATES("", "gen-templates"),
        UPGRADE_TEMPLATES("", "upgrade-templates"),
        EXAMPLE_MODIFIERS("modifiers", "example-modifiers"),

        // Default language files -> /MMOItems/language
        LORE_FORMAT("language", "lore-format"),
        STATS("language", "stats"),

        // Station layouts
        DEFAULT_LAYOUT("layouts", "default"),
        EXPANDED_LAYOUT("layouts", "expanded"),

        // Default item config files -> /MMOItems/item
        ARMOR,
        AXE,
        BLOCK,
        BOW,
        CATALYST,
        CONSUMABLE,
        CROSSBOW,
        DAGGER,
        GAUNTLET,
        GEM_STONE,
        GREATAXE,
        GREATHAMMER,
        GREATSTAFF,
        GREATSWORD,
        HALBERD,
        HAMMER,
        KATANA,
        LANCE,
        LONG_SWORD,
        MATERIAL,
        MISCELLANEOUS,
        MUSKET,
        OFF_CATALYST,
        ORNAMENT,
        SHIELD,
        SPEAR,
        STAFF,
        SWORD,
        TALISMAN,
        THRUSTING_SWORD,
        TOME,
        TOOL,
        WAND,
        WHIP;

        private final String folderPath, fileName;

        /**
         * Allows to use the checkFile() method while not
         * loading it automatically e.g item-types.yml
         */
        private final boolean manual;

        DefaultFile() {
            this.fileName = name().toLowerCase() + ".yml";
            this.folderPath = "item";
            this.manual = false;
        }

        DefaultFile(String folderPath, String fileName) {
            this(folderPath, fileName, false);
        }

        DefaultFile(String folderPath, String fileName, boolean manual) {
            this.folderPath = folderPath;
            this.fileName = fileName + ".yml";
            this.manual = manual;
        }

        public boolean isAutomatic() {
            return !manual;
        }

        public File getFile() {
            return new File(MMOItems.plugin.getDataFolder() + (folderPath.equals("") ? "" : "/" + folderPath), fileName);
        }

        public void checkFile() {
            File file = getFile();
            if (!file.exists())
                try {
                    if (!new YamlConverter(file).convert()) {
                        Files.copy(MMOItems.plugin.getResource("default/" + (folderPath.isEmpty() ? "" : folderPath + "/") + fileName), file.getAbsoluteFile().toPath());
                    }

                } catch (IOException exception) {
                    exception.printStackTrace();
                }
        }
    }

    public static class YamlConverter {
        private final File file;

        private final String fileName;

        public YamlConverter(File newConfig) {
            this.file = newConfig;
            this.fileName = newConfig.getName();
        }

        public boolean convert() throws IOException {
            if (!file.exists())
                if (fileName.equalsIgnoreCase("block.yml") && new File(MMOItems.plugin.getDataFolder(), "custom-blocks.yml").exists()) {
                    // creates the file
                    if (file.createNewFile()) {
                        YamlConfiguration config = YamlConfiguration.loadConfiguration(new File(MMOItems.plugin.getDataFolder(), "custom-blocks.yml"));

                        for (String id : config.getKeys(false)) {
                            ConfigurationSection section = config.getConfigurationSection(id);
                            section.set("material", "STONE"); // adds material
                            section.set("block-id", Integer.parseInt(id)); // adds
                            // block
                            // id
                            for (String node : section.getKeys(false)) {
                                Object value = section.get(node);
                                if (node.equalsIgnoreCase("display-name")) { // converts
                                    // name
                                    // format
                                    section.set("display-name", null);
                                    section.set("name", value);
                                }
                            }
                        }
                        config.save(file);
                        MMOItems.plugin.getLogger().log(Level.CONFIG, "Successfully converted custom-blocks.yml");
                        return true;
                    }
                }
            return false;
        }
    }
}
