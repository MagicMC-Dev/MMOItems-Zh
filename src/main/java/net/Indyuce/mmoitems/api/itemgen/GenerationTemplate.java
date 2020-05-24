package net.Indyuce.mmoitems.api.itemgen;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.itemgen.tier.RolledTier;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.stat.type.ItemStat;

public class GenerationTemplate {
	private final String id;
	private final Type type;

	// base item data
	private final Map<ItemStat, RandomStatData> base = new HashMap<>();

	private final Set<GenerationModifier> modifiers = new LinkedHashSet<>();
	private final Set<TemplateOption> options = new HashSet<>();

	public GenerationTemplate(ConfigurationSection config) {
		Validate.notNull(config, "Could not load item gen template config");

		this.id = config.getName().toUpperCase().replace("-", "_").replace(" ", "_");

		Validate.isTrue(config.contains("type"), "Could not find item gen type");
		String typeFormat = config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_");
		Validate.isTrue(MMOItems.plugin.getTypes().has(typeFormat));
		type = MMOItems.plugin.getTypes().get(typeFormat);

		if (config.contains("option"))
			for (String key : config.getConfigurationSection("option").getKeys(false)) {
				TemplateOption opt = TemplateOption.valueOf(key.toUpperCase().replace("-", "_").replace(" ", "_"));
				if (config.getBoolean("option." + key))
					options.add(opt);
			}

		if (config.contains("modifiers"))
			for (String key : config.getConfigurationSection("modifiers").getKeys(false))
				try {
					modifiers.add(new GenerationModifier(MMOItems.plugin.getItemGenerator(),
							config.getConfigurationSection("modifiers." + key)));
				} catch (IllegalArgumentException exception) {
					MMOItems.plugin.getLogger().log(Level.INFO, "An error occured while trying to load modifier '" + key
							+ "' from item gen template '" + id + "': " + exception.getMessage());
				}

		Validate.notNull(config.getConfigurationSection("base"), "Could not find base item data");
		for (String key : config.getConfigurationSection("base").getKeys(false))
			try {
				String id = key.toUpperCase().replace("-", "_");
				Validate.isTrue(MMOItems.plugin.getStats().has(id), "Could not find stat with ID '" + id + "'");

				ItemStat stat = MMOItems.plugin.getStats().get(id);
				base.put(stat, stat.whenInitializedGeneration(config.get("base." + key)));
			} catch (IllegalArgumentException exception) {
				MMOItems.plugin.getLogger().log(Level.INFO, "An error occured while trying to load base item data '"
						+ key + "' from item gen template '" + id + "': " + exception.getMessage());
			}
	}

	public Map<ItemStat, RandomStatData> getBaseItemData() {
		return base;
	}

	public Set<GenerationModifier> getModifiers() {
		return modifiers;
	}

	public Type getType() {
		return type;
	}

	public String getId() {
		return id;
	}

	public boolean hasOption(TemplateOption option) {
		return options.contains(option);
	}

	public GeneratedItemBuilder newBuilder(RPGPlayer player) {
		int itemLevel = MMOItems.plugin.getItemGenerator().rollLevel(player.getLevel());
		RolledTier itemTier = MMOItems.plugin.getItemGenerator().rollTier(itemLevel);
		return newBuilder(itemLevel, itemTier);
	}

	public GeneratedItemBuilder newBuilder(int itemLevel, RolledTier itemTier) {
		return new GeneratedItemBuilder(this, itemLevel, itemTier);
	}

	public enum TemplateOption {

		/*
		 * when the item is being generated, modifiers are rolled in a random order so
		 * you never the same modifiers again and again
		 */
		ROLL_MODIFIER_CHECK_ORDER;
	}
}
