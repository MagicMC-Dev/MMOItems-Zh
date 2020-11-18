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
import org.bukkit.plugin.java.JavaPlugin;

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
	// Abilities from ThirdParty plugins
	private final Map<String, Ability> tpAbilities = new HashMap<>();
	
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
	 * @return Returns all known abilities, both from MI, MM and Third Party plugins.  
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

	/**
	 * @return Returns all known THIRD PARTY abilities.
	 */
	public Collection<Ability> getAllThirdPartyAbilities() {
		return tpAbilities.values();
	}

	/**
	 * Add a custom ability into MMOItems.
	 * Used for Third Party abilities only.
	 * 
	 * @param ability - A class that extends {@link Ability}
	 */
	public void registerAbility(Ability ability) {
		if (registerAbility(ability, false, true))
			MMOItems.plugin.getLogger().log(Level.INFO, "Loaded third party ability: " + ability.getName() + " from " + JavaPlugin.getProvidingPlugin(ability.getClass()).getName() + ".");
	}

	/**
	 * Add custom abilities into MMOItems.
	 * Used for Third Party abilities only.
	 * Same as {@link #registerAbility(Ability ability)}
	 * but for multiple abilities.
	 * 
	 * @param ability - A class that extends {@link Ability}
	 */
	public void registerAbilities(Ability... abilities) {
		int count = 0;
		for (Ability ability : abilities) {
			if (!registerAbility(ability, false, true))
				continue;
			count++;
		}
		
		MMOItems.plugin.getLogger().log(Level.INFO, "Loaded " + count + " third party abilities from " + JavaPlugin.getProvidingPlugin(abilities[0].getClass()).getName() + ".");
	}

	/**
	 * Used only internally.
	 */
	protected boolean registerAbility(Ability ability, boolean fromMM, boolean fromTP) {
		if (registrationIsDone && !fromTP) {
			MMOItems.plugin.getLogger().log(Level.WARNING,
					"Failed attempt to register ability " + ability.getID() + ". Make sure abilities are registered when MI is loading.");
			return false;
		}

		if (!ability.isEnabled()) {
			MMOItems.plugin.getLogger().log(Level.WARNING, "Cannot register disabled ability " + ability.getID() + ".");
			return false;
		}

		if (hasAbility(ability.getID())) {
			MMOItems.plugin.getLogger().log(Level.WARNING, "Ability " + ability.getID() + " is already registered!");
			return false;
		}

		if (ability instanceof Listener)
			Bukkit.getPluginManager().registerEvents((Listener) ability, MMOItems.plugin);
		
		// Add to MM/MI or TP ability list
		if (fromTP)
			tpAbilities.put(ability.getID(), ability);
		else if (fromMM)
			mmAbilities.put(ability.getID(), ability);
		else
			miAbilities.put(ability.getID(), ability);
		
		// Add to all ability list
		abilities.put(ability.getID(), ability);
		
		return true;
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
						registerAbility(ability, false, false);
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
							YamlConfiguration.loadConfiguration(file)), true, false);
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
