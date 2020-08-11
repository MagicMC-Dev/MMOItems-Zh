package net.Indyuce.mmoitems.manager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.potion.PotionEffectType;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.ability.Ability;
import net.Indyuce.mmoitems.api.ability.Ability.CastingMode;
import net.Indyuce.mmoitems.api.item.internal.ConfigItem;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.stat.LuteAttackEffectStat.LuteAttackEffect;
import net.Indyuce.mmoitems.stat.StaffSpiritStat.StaffSpirit;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.util.AltChar;

public class ConfigManager {

	// cached config files
	private ConfigFile abilities, items, loreFormat, messages, potionEffects, stats, attackEffects;

	// cached config options
	public boolean abilityPlayerDamage, dodgeKnockbackEnabled, replaceMushroomDrops, worldGenEnabled, upgradeRequirementsCheck;
	public String healIndicatorFormat, damageIndicatorFormat, abilitySplitter;
	public DecimalFormat healIndicatorDecimalFormat, damageIndicatorDecimalFormat;
	public double dodgeKnockbackForce, soulboundBaseDamage, soulboundPerLvlDamage, levelSpread;
	public NumericStatFormula defaultItemCapacity;

	private static final String[] fileNames = { "abilities", "messages", "potion-effects", "stats", "items", "attack-effects" };
	private static final String[] languages = { "french", "chinese", "spanish", "russian", "polish" };

	// try to setup non existing languages
	public ConfigManager() {

		mkdir("item");
		mkdir("dynamic");
		mkdir("language");
		mkdir("modifiers");

		File craftingStationsFolder = new File(MMOItems.plugin.getDataFolder() + "/crafting-stations");
		if (!craftingStationsFolder.exists()) {
			craftingStationsFolder.mkdir();
			try {
				JarFile jarFile = new JarFile(MMOItems.plugin.getJarFile());
				for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
					String name = entries.nextElement().getName();
					if (name.startsWith("default/crafting-stations/") && name.length() > "default/crafting-stations/".length())
						Files.copy(MMOItems.plugin.getResource(name),
								new File(MMOItems.plugin.getDataFolder() + "/crafting-stations", name.split("\\/")[2]).toPath());
				}
				jarFile.close();
			} catch (IOException exception) {
				MMOItems.plugin.getLogger().log(Level.WARNING, "Could not load default crafting stations.");
			}
		}

		for (String language : languages) {
			File languageFolder = new File(MMOItems.plugin.getDataFolder() + "/language/" + language);
			if (!languageFolder.exists())
				languageFolder.mkdir();

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
		}

		// load files with default configuration
		for (DefaultFile file : DefaultFile.values())
			if (file.isAutomatic())
				file.checkFile();

		/*
		 * setup /item files after generating the default /item files otherwise
		 * they will be empty!
		 */
		MMOItems.plugin.getTypes().getAll().forEach(type -> type.getConfigFile().setup());

		ConfigFile items = new ConfigFile("/language", "items");
		for (ConfigItem item : ConfigItem.values) {
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
		for (Ability ability : MMOItems.plugin.getAbilities().getAll()) {
			String path = ability.getLowerCaseID();
			if (!abilities.getConfig().getKeys(true).contains("ability." + path))
				abilities.getConfig().set("ability." + path, ability.getName());

			for (String modifier : ability.getModifiers())
				if (!abilities.getConfig().getKeys(true).contains("modifier." + modifier))
					abilities.getConfig().set("modifier." + modifier, MMOUtils.caseOnWords(modifier.replace("-", " ")));
		}
		for (CastingMode mode : CastingMode.values())
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
		 * only load config files after they have been initialized (above) so
		 * they do not crash the first time they generate and so we do not have
		 * to restart the server
		 */
		reload();
	}

	public void reload() {
		MMOItems.plugin.reloadConfig();

		abilities = new ConfigFile("/language", "abilities");
		items = new ConfigFile("/language", "items");
		loreFormat = new ConfigFile("/language", "lore-format");
		messages = new ConfigFile("/language", "messages");
		potionEffects = new ConfigFile("/language", "potion-effects");
		stats = new ConfigFile("/language", "stats");
		attackEffects = new ConfigFile("/language", "attack-effects");

		/*
		 * reload cached config options for quicker access - these options are
		 * used in runnables, it is thus better to cache them
		 */
		replaceMushroomDrops = MMOItems.plugin.getConfig().getBoolean("custom-blocks.replace-mushroom-drops");
		worldGenEnabled = MMOItems.plugin.getConfig().getBoolean("custom-blocks.enable-world-gen");
		abilityPlayerDamage = MMOItems.plugin.getConfig().getBoolean("ability-player-damage");
		healIndicatorFormat = MMOLib.plugin.parseColors(MMOItems.plugin.getConfig().getString("game-indicators.heal.format"));
		damageIndicatorFormat = MMOLib.plugin.parseColors(MMOItems.plugin.getConfig().getString("game-indicators.damage.format"));
		healIndicatorDecimalFormat = new DecimalFormat(MMOItems.plugin.getConfig().getString("game-indicators.heal.decimal-format"));
		damageIndicatorDecimalFormat = new DecimalFormat(MMOItems.plugin.getConfig().getString("game-indicators.damage.decimal-format"));
		abilitySplitter = getStatFormat("ability-splitter");
		dodgeKnockbackForce = MMOItems.plugin.getConfig().getDouble("mitigation.dodge.knockback.force");
		dodgeKnockbackEnabled = MMOItems.plugin.getConfig().getBoolean("mitigation.dodge.knockback.enabled");
		soulboundBaseDamage = MMOItems.plugin.getConfig().getDouble("soulbound.damage.base");
		soulboundPerLvlDamage = MMOItems.plugin.getConfig().getDouble("soulbound.damage.per-lvl");
		upgradeRequirementsCheck = MMOItems.plugin.getConfig().getBoolean("item-upgrade-requirements-check");
		levelSpread = MMOItems.plugin.getConfig().getDouble("item-level-spread");

		try {
			defaultItemCapacity = new NumericStatFormula(MMOItems.plugin.getConfig().getConfigurationSection("default-item-capacity"));
		} catch (IllegalArgumentException exception) {
			defaultItemCapacity = new NumericStatFormula(5, .05, .1, .3);
			MMOItems.plugin.getLogger().log(Level.INFO,
					"An error occured while trying to load default capacity formula for the item generator, using default: "
							+ exception.getMessage());
		}

		for (ConfigItem item : ConfigItem.values)
			item.update(items.getConfig().getConfigurationSection(item.getId()));
	}

	public boolean isBlacklisted(Material material) {
		return MMOItems.plugin.getConfig().getStringList("block-blacklist").contains(material.name());
	}

	public String getStatFormat(String path) {
		String found = stats.getConfig().getString(path);
		return found == null ? "<TranslationNotFound:" + path + ">" : found;
	}

	public String getMessage(String path) {
		String found = messages.getConfig().getString(path);
		return MMOLib.plugin.parseColors(found == null ? "<MessageNotFound:" + path + ">" : found);
	}

	public String getAbilityName(Ability ability) {
		return abilities.getConfig().getString("ability." + ability.getLowerCaseID());
	}

	public String getCastingModeName(CastingMode mode) {
		return abilities.getConfig().getString("cast-mode." + mode.getLowerCaseId());
	}

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
	 * Creates an empty directory in the MMOItems plugin folder if it does not
	 * exist
	 * 
	 * @param path
	 *            The path of your folder
	 */
	private void mkdir(String path) {
		File folder = new File(MMOItems.plugin.getDataFolder() + "/" + path);
		if (!folder.exists())
			folder.mkdir();
	}

	/*
	 * all config files that have a default configuration are stored here, they
	 * get copied into the plugin folder when the plugin enables
	 */
	public enum DefaultFile {

		// default general config files -> /MMOItems
		ITEM_TIERS("item-tiers.yml", "", "item-tiers.yml"),
		ITEM_TYPES("item-types.yml", "", "item-types.yml", true),
		DROPS("drops.yml", "", "drops.yml"),
		ITEM_SETS("item-sets.yml", "", "item-sets.yml"),
		UPGRADE_TEMPLATES("upgrade-templates.yml", "", "upgrade-templates.yml"),
		EXAMPLE_MODIFIERS("modifiers/example-modifiers.yml", "modifiers", "example-modifiers.yml"),

		// default language files -> /MMOItems/language
		LORE_FORMAT("lore-format.yml", "language", "lore-format.yml"),
		STATS("stats.yml", "language", "stats.yml"),

		// default item config files -> /MMOItems/item
		ARMOR("item/armor.yml", "item", "armor.yml"),
		AXE("item/axe.yml", "item", "axe.yml"),
		BLOCK("item/block.yml", "item", "block.yml"),
		BOW("item/bow.yml", "item", "bow.yml"),
		CATALYST("item/catalyst.yml", "item", "catalyst.yml"),
		CONSUMABLE("item/consumable.yml", "item", "consumable.yml"),
		DAGGER("item/dagger.yml", "item", "dagger.yml"),
		GEM_STONE("item/gem_stone.yml", "item", "gem_stone.yml"),
		GREATSTAFF("item/greatstaff.yml", "item", "greatstaff.yml"),
		GREATSWORD("item/greatsword.yml", "item", "greatsword.yml"),
		HALBERD("item/halberd.yml", "item", "halberd.yml"),
		HAMMER("item/hammer.yml", "item", "hammer.yml"),
		LANCE("item/lance.yml", "item", "lance.yml"),
		MATERIAL("item/material.yml", "item", "material.yml"),
		MISCELLANEOUS("item/miscellaneous.yml", "item", "miscellaneous.yml"),
		SHIELD("item/shield.yml", "item", "shield.yml"),
		STAFF("item/staff.yml", "item", "staff.yml"),
		SWORD("item/sword.yml", "item", "sword.yml"),
		TOME("item/tome.yml", "item", "tome.yml"),
		TOOL("item/tool.yml", "item", "tool.yml"),
		WAND("item/wand.yml", "item", "wand.yml");

		private final String folderPath, fileName, resourceName;

		/*
		 * allows to use the checkFile() method while not loading it
		 * automatically e.g item-types.yml
		 */
		private final boolean manual;

		private DefaultFile(String resourceName, String folderPath, String fileName) {
			this(resourceName, folderPath, fileName, false);
		}

		private DefaultFile(String resourceName, String folderPath, String fileName, boolean manual) {
			this.resourceName = resourceName;
			this.folderPath = folderPath;
			this.fileName = fileName;
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
						Files.copy(MMOItems.plugin.getResource("default/" + resourceName), file.getAbsoluteFile().toPath());
					}

				} catch (IOException exception) {
					exception.printStackTrace();
				}
		}
	}

	public static class YamlConverter {
		private File file;

		private final String fileName;

		public YamlConverter(File newConfig) {
			this.file = newConfig;
			this.fileName = newConfig.getName();

		}

		public boolean convert() throws IOException {
			if (!file.exists())
				if (fileName.equalsIgnoreCase("block.yml") && new File(MMOItems.plugin.getDataFolder(), "custom-blocks.yml").exists()) { // this
																																			// converts
																																			// old
																																			// custom-blocks.yml
					file.createNewFile(); // creates the file

					YamlConfiguration oldConfig = YamlConfiguration.loadConfiguration(new File(MMOItems.plugin.getDataFolder(), "custom-blocks.yml"));

					YamlConfiguration newConfig = oldConfig;

					for (String id : oldConfig.getKeys(false)) {
						ConfigurationSection section = newConfig.getConfigurationSection(id);
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
					newConfig.save(file);
					MMOItems.plugin.getLogger().log(Level.CONFIG, "Successfully converted custom-blocks.yml");
					return true;
				}
			return false;
		}
	}
}
