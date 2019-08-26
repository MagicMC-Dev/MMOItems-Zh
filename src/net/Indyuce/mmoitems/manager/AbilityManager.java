package net.Indyuce.mmoitems.manager;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.comp.mythicmobs.MythicMobsAbility;

public class AbilityManager {
	private final Map<String, Ability> abilities = new HashMap<>();
	private boolean registration = true;

	public Ability getAbility(String id) {
		return abilities.get(id);
	}

	public boolean hasAbility(String id) {
		return abilities.containsKey(id);
	}

	public Collection<Ability> getAll() {
		return abilities.values();
	}

	public Set<String> getAbilityKeys() {
		return abilities.keySet();
	}

	public void registerAbility(Ability ability) {
		if (!registration) {
			MMOItems.plugin.getLogger().log(Level.INFO, "Failed attempt to register ability " + ability.getID() + ". Make sure you register your abilities when MI is loaded.");
			return;
		}

		if (ability.isEnabled()) {
			if (ability instanceof Listener)
				Bukkit.getPluginManager().registerEvents((Listener) ability, MMOItems.plugin);
			abilities.put(ability.getID(), ability);
		}
	}

	public void registerDefaultAbilities() {
		try {
			JarFile file = new JarFile(MMOItems.plugin.getJarFile());
			for (Enumeration<JarEntry> enu = file.entries(); enu.hasMoreElements();) {
				String name = enu.nextElement().getName().replace("/", ".");
				if (!name.contains("$") && name.endsWith(".class") && name.startsWith("net.Indyuce.mmoitems.ability."))
					registerAbility((Ability) Class.forName(name.substring(0, name.length() - 6)).newInstance());
			}
			file.close();
		} catch (IOException | InstantiationException | IllegalAccessException | ClassNotFoundException exception) {
			exception.printStackTrace();
		}

		File mythicMobs = new File(MMOItems.plugin.getDataFolder() + "/dynamic/mythic-mobs-abilities");
		if (!mythicMobs.exists())
			mythicMobs.mkdir();

		/*
		 * load MythicMobs addon skills
		 */
		if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
			int count = 0;
			for (File file : mythicMobs.listFiles()) {
				try {
					registerAbility(new MythicMobsAbility(file.getName().substring(0, file.getName().length() - 4), YamlConfiguration.loadConfiguration(file)));
					count++;
				} catch (IllegalArgumentException exception) {
					MMOItems.plugin.getLogger().log(Level.WARNING, "Could not load ability from " + file.getName() + ": " + exception.getMessage());
				}
			}
			if (count > 0)
				MMOItems.plugin.getLogger().log(Level.INFO, "Loaded " + count + " extra MM abilities");
		}
	}

	public void stopRegistration() {
		registration = false;
	}
}
