package net.Indyuce.mmoitems.manager;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.lang3.Validate;
import org.bukkit.configuration.file.YamlConfiguration;

import net.Indyuce.mmoitems.MMOItems;

public class LoreFormatManager {
	private final Map<String, List<String>> formats = new HashMap<>();

	public void reload() {
		formats.clear();
		File dir = new File(MMOItems.plugin.getDataFolder() + "/language/lore-formats");
		for (File file : dir.listFiles())
			try {
				YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
				Validate.isTrue(config.isList("lore-format"), "Invalid lore-format! (" + file.getName() + ")");
				formats.put(file.getName().substring(0, file.getName().length() - 4), config.getStringList("lore-format"));
			} catch (IllegalArgumentException exception) {
				MMOItems.plugin.getLogger().log(Level.WARNING, "Could not load layout '" + file.getName() + "': " + exception.getMessage());
			}
	}

	public boolean hasFormat(String id) {
		return formats.containsKey(id);
	}

	public Collection<List<String>> getFormats() {
		return formats.values();
	}

	public List<String> getFormat(String id) {
		if(!hasFormat(id)) return MMOItems.plugin.getLanguage().getDefaultLoreFormat();
		return formats.get(id);
	}
}
