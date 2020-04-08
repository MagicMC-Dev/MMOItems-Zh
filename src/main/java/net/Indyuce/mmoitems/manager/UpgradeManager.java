package net.Indyuce.mmoitems.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.UpgradeTemplate;

public class UpgradeManager {
	private Map<String, UpgradeTemplate> templates = new HashMap<>();

	public UpgradeManager() {
		reload();
	}
	
	public void reload() {
		templates.clear();

		FileConfiguration config = new ConfigFile("upgrade-templates").getConfig();
		for (String key : config.getKeys(false))
			try {
				registerTemplate(new UpgradeTemplate(config.getConfigurationSection(key)));
			} catch (IllegalArgumentException exception) {
				MMOItems.plugin.getLogger().log(Level.WARNING, "Could not load upgrade template '" + key + "': " + exception.getMessage());
			}
	}

	public Collection<UpgradeTemplate> getAll() {
		return templates.values();
	}

	public UpgradeTemplate getTemplate(String id) {
		return templates.get(id);
	}

	public boolean hasTemplate(String id) {
		return templates.containsKey(id);
	}

	public void registerTemplate(UpgradeTemplate template) {
		templates.put(template.getId(), template);
	}
}
