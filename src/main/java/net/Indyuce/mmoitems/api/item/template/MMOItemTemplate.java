package net.Indyuce.mmoitems.api.item.template;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.ItemReference;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;

public class MMOItemTemplate implements ItemReference {
	private final Type type;
	private final String id;

	// base item data
	private final Map<ItemStat, RandomStatData> base = new HashMap<>();

	private final Map<String, TemplateModifier> modifiers = new LinkedHashMap<>();
	private final Set<TemplateOption> options = new HashSet<>();

	/**
	 * Public constructor which can be used to register extra item templates
	 * using other addons or plugins
	 * 
	 * @param type
	 *            The item type of your template
	 * @param id
	 *            The template identifier, it's ok if two templates with
	 *            different item types share the same ID
	 */
	public MMOItemTemplate(Type type, String id) {
		this.type = type;
		this.id = id;
	}

	/**
	 * Used to load mmoitem templates from config files
	 * 
	 * @param type
	 *            The item type of your template
	 * @param config
	 *            The config file read to load the template
	 */
	public MMOItemTemplate(Type type, ConfigurationSection config) {
		Validate.notNull(config, "Could not load template config");

		this.type = type;
		this.id = config.getName().toUpperCase().replace("-", "_").replace(" ", "_");

		if (config.contains("option"))
			for (String key : config.getConfigurationSection("option").getKeys(false)) {
				TemplateOption opt = TemplateOption.valueOf(key.toUpperCase().replace("-", "_").replace(" ", "_"));
				if (config.getBoolean("option." + key))
					options.add(opt);
			}

		if (config.contains("modifiers"))
			for (String key : config.getConfigurationSection("modifiers").getKeys(false))
				try {
					TemplateModifier modifier = new TemplateModifier(MMOItems.plugin.getTemplates(),
							config.getConfigurationSection("modifiers." + key));
					modifiers.put(modifier.getId(), modifier);
				} catch (IllegalArgumentException exception) {
					MMOItems.plugin.getLogger().log(Level.INFO,
							"Could not load modifier '" + key + "' from item template '" + type.getId() + "." + id + "': " + exception.getMessage());
				}

		Validate.notNull(config.getConfigurationSection("base"), "Could not find base item data");
		for (String key : config.getConfigurationSection("base").getKeys(false))
			try {
				String id = key.toUpperCase().replace("-", "_");
				Validate.isTrue(MMOItems.plugin.getStats().has(id), "Could not find stat with ID '" + id + "'");

				ItemStat stat = MMOItems.plugin.getStats().get(id);
				RandomStatData data = stat.whenInitialized(config.get("base." + key));
				if (data != null)
					base.put(stat, data);
			} catch (IllegalArgumentException exception) {
				MMOItems.plugin.getLogger().log(Level.INFO, "Could not load base item data '" + key + "' from item template '" + type.getId() + "."
						+ id + "': " + exception.getMessage());
			}
	}

	public Map<ItemStat, RandomStatData> getBaseItemData() {
		return base;
	}

	public Map<String, TemplateModifier> getModifiers() {
		return modifiers;
	}

	public boolean hasModifier(String id) {
		return modifiers.containsKey(id);
	}

	public TemplateModifier getModifier(String id) {
		return modifiers.get(id);
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public String getId() {
		return id;
	}

	public boolean hasOption(TemplateOption option) {
		return options.contains(option);
	}

	/**
	 * @param player
	 *            The rpg info about the player whom you want to give a random
	 *            item to
	 * @return A random item builder which scales on the player's level.
	 */
	public MMOItemBuilder newBuilder(RPGPlayer player) {
		int itemLevel = MMOItems.plugin.getTemplates().rollLevel(player.getLevel());
		ItemTier itemTier = MMOItems.plugin.getTemplates().rollTier();
		return newBuilder(itemLevel, itemTier);
	}

	public MMOItemBuilder newBuilder(int itemLevel, ItemTier itemTier) {
		return new MMOItemBuilder(this, itemLevel, itemTier);
	}

	public enum TemplateOption {

		/*
		 * when the item is being generated, modifiers are rolled in a random
		 * order so you never the same modifiers again and again
		 */
		ROLL_MODIFIER_CHECK_ORDER;
	}
}
