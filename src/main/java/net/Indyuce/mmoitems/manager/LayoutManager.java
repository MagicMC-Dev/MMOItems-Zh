package net.Indyuce.mmoitems.manager;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.configuration.file.YamlConfiguration;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.crafting.Layout;

public class LayoutManager {
	private final Map<String, Layout> layouts = new HashMap<>();

	public void reload() {
		layouts.clear();
		for (File file : new File(MMOItems.plugin.getDataFolder() + "/layouts").listFiles())
			try {
				Layout layout = new Layout(file.getName().substring(0, file.getName().length() - 4), YamlConfiguration.loadConfiguration(file));
				layouts.put(layout.getId(), layout);
			} catch (IllegalArgumentException exception) {
				MMOItems.plugin.getLogger().log(Level.WARNING, "Could not load layout '" + file.getName() + "': " + exception.getMessage());
			}
	}

	public boolean hasLayout(String id) {
		return layouts.containsKey(id);
	}

	public Collection<Layout> getLayouts() {
		return layouts.values();
	}

	public Layout getLayout(String id) {
		return layouts.getOrDefault(id, layouts.get("default"));
	}
}
