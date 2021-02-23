package net.Indyuce.mmoitems.manager;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.UpgradeTemplate;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class UpgradeManager implements Reloadable {
	private final Map<String, UpgradeTemplate> templates = new HashMap<>();

	public UpgradeManager() {
		reload();
	}
	
	public void reload() {
		templates.clear();

		FileConfiguration config = new ConfigFile("upgrade-templates").getConfig();
		for (String key : config.getKeys(false)) {

			// Register
			registerTemplate(new UpgradeTemplate(config.getConfigurationSection(key)));
		}
	}

	public Collection<UpgradeTemplate> getAll() {
		return templates.values();
	}

	/**
	 * Get the <code>UpgradeTemplate</code> of this name.
	 * @return <code>null</code> if there is no such template loaded.
	 */
	@Nullable public UpgradeTemplate getTemplate(@NotNull String id) {
		return templates.get(id);
	}

	public boolean hasTemplate(String id) {
		return templates.containsKey(id);
	}

	public void registerTemplate(UpgradeTemplate template) {
		templates.put(template.getId(), template);
	}
}
