package net.Indyuce.mmoitems.manager;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ability.Ability;
import net.Indyuce.mmoitems.comp.mythicmobs.MythicMobsAbility;

public class AbilityManager {
	// All abilities
	private final Map<String, Ability> abilities = new HashMap<>();
	// Abilities from MMOItems
	private final Map<String, Ability> miAbilities = new HashMap<>();
	// Abilities from MythicMobs
	private final Map<String, Ability> mmAbilities = new HashMap<>();
	
	private boolean registrationIsDone = false;

	public Ability getAbility(String id) {
		return abilities.get(id);
	}

	public boolean hasAbility(String id) {
		return abilities.containsKey(id);
	}
	
	/**
	 * @deprecated use {@link #getAllAbilities()} instead.
	 * Currently returns the same thing but is a better more futureproof name.
	 */
	@Deprecated
	public Collection<Ability> getAll() {
		return abilities.values();
	}
	
	/**
	 * @return Returns all known abilities, both from MI and MM.  
	 */
	public Collection<Ability> getAllAbilities() {
		return abilities.values();
	}

	/**
	 * @return Returns all known MMOITEMS abilities.
	 */
	public Collection<Ability> getAllMMOItemsAbilities() {
		return miAbilities.values();
	}

	/**
	 * @return Returns all known MYTHICMOBS abilities.
	 */
	public Collection<Ability> getAllMythicMobsAbilities() {
		return mmAbilities.values();
	}

	public void registerAbility(Ability ability, boolean mmSkill) {
		if (registrationIsDone) {
			MMOItems.plugin.getLogger().log(Level.INFO,
					"Failed attempt to register ability " + ability.getID() + ". Make sure abilities are registered when MI is loading.");
			return;
		}

		if (!ability.isEnabled()) {
			MMOItems.plugin.getLogger().log(Level.INFO, "Cannot register disabled ability " + ability.getID() + ".");
			return;
		}

		if (ability instanceof Listener)
			Bukkit.getPluginManager().registerEvents((Listener) ability, MMOItems.plugin);
		
		// Add to MM or MI ability list
		if (mmSkill)
			mmAbilities.put(ability.getID(), ability);
		else
			miAbilities.put(ability.getID(), ability);
		
		// Add to all ability list
		abilities.put(ability.getID(), ability);
	}

	public void initialize() {
		// Load MMOItems abilities
		try {
			JarFile file = new JarFile(MMOItems.plugin.getJarFile());
			for (Enumeration<JarEntry> enu = file.entries(); enu.hasMoreElements();) {
				String name = enu.nextElement().getName().replace("/", ".");
				if (!name.contains("$") && name.endsWith(".class") && name.startsWith("net.Indyuce.mmoitems.ability.")) {
					Ability ability = (Ability) Class.forName(name.substring(0, name.length() - 6)).newInstance();
					if (ability.isEnabled())
						registerAbility(ability, false);
				}
			}
			file.close();
		} catch (IOException | InstantiationException | IllegalAccessException | ClassNotFoundException exception) {
			exception.printStackTrace();
		}

		File mythicMobs = new File(MMOItems.plugin.getDataFolder() + "/dynamic/mythic-mobs-abilities");
		if (!mythicMobs.exists())
			mythicMobs.mkdirs();

		// Load MythicMobs addon skills
		if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
			int count = 0;
			for (File file : mythicMobs.listFiles()) {
				try {
					registerAbility(new MythicMobsAbility(file.getName().substring(0, file.getName().length() - 4),
							YamlConfiguration.loadConfiguration(file)), true);
					count++;
				} catch (IllegalArgumentException exception) {
					MMOItems.plugin.getLogger().log(Level.WARNING, "Could not load ability from " + file.getName() + ": " + exception.getMessage());
				}
			}
			if (count > 0)
				MMOItems.plugin.getLogger().log(Level.INFO, "Loaded " + count + " extra MythicMobs abilities");
		}

		registrationIsDone = true;
	}
}
