package net.Indyuce.mmoitems.manager;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.itemgen.GenerationTemplate;

public class ItemGenManager {
	private final Map<String, GenerationTemplate> templates = new HashMap<>();

	public ItemGenManager() {
		reload();
	}

	public Collection<GenerationTemplate> getTemplates() {
		return templates.values();
	}

	public boolean hasTemplate(String id) {
		return templates.containsKey(id);
	}

	public GenerationTemplate getTemplate(String id) {
		return templates.get(id);
	}

	public void reload() {
		templates.clear();
		for (File file : new File(MMOItems.plugin.getDataFolder() + "/generator/templates").listFiles()) {
			FileConfiguration config = YamlConfiguration.loadConfiguration(file);
			for (String key : config.getKeys(false))
				try {
					GenerationTemplate template = new GenerationTemplate(config.getConfigurationSection(key));
					templates.put(template.getId(), template);
				} catch (IllegalArgumentException exception) {
					exception.printStackTrace();
					MMOItems.plugin.getLogger().log(Level.INFO,
							"An error occured loading item gen template '" + key + "': " + exception.getMessage());
				}
		}
	}
}
