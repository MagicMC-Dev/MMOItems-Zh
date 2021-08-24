package net.Indyuce.mmoitems.manager;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.ability.Ability;
import net.Indyuce.mmoitems.comp.mythicmobs.skill.MythicMobsAbility;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

@SuppressWarnings("unused")
public class AbilityManager {
	private final Map<String, Ability> abilities = new HashMap<>();

	private boolean registration = true;

	public Ability getAbility(String id) {
		return abilities.get(id);
	}

	public boolean hasAbility(String id) {
		return abilities.containsKey(id);
	}

	/**
	 * @return Collection of all active abilities
	 */
	public Collection<Ability> getAll() {
		return abilities.values();
	}

	/**
	 * Add multiple abilities at the same time
	 * but for multiple abilities.
	 *
	 * @param abilities - Refer to {@link #registerAbility(Ability ability)}
	 */
	public void registerAbilities(Ability... abilities) {
		for (Ability ability : abilities)
			registerAbility(ability);
	}

	/**
	 * Registers an ability in MMOItems. This must be called before MMOItems enables,
	 * therefore either using a loadbefore of MMOItems and while the plugin enables,
	 * or using a dependency and usign #onLoad().
	 * <p>
	 * This method does NOT register listeners.
	 * <p>
	 * Throws an IAE if anything goes wrong.
	 *
	 * @param ability Ability to register
	 */
	public void registerAbility(Ability ability) {
		Validate.isTrue(registration, "Ability registration is disabled");
		Validate.isTrue(!hasAbility(ability.getID()), "An ability is already registered with the same ID");

		// Add to all ability list
		abilities.put(ability.getID(), ability);
	}

	public void loadPluginAbilities() {

		// Load MMOItems default abilities
		try {
			JarFile file = new JarFile(MMOItems.plugin.getJarFile());
			for (Enumeration<JarEntry> enu = file.entries(); enu.hasMoreElements(); ) {
				String name = enu.nextElement().getName().replace("/", ".");
				if (!name.contains("$") && name.endsWith(".class") && name.startsWith("net.Indyuce.mmoitems.ability.list.")) {
					Ability ability = (Ability) Class.forName(name.substring(0, name.length() - 6)).newInstance();
					registerAbility(ability);
					if (ability instanceof Listener)
						Bukkit.getPluginManager().registerEvents((Listener) ability, MMOItems.plugin);
				}
			}
			file.close();
		} catch (IOException | InstantiationException | IllegalAccessException | ClassNotFoundException exception) {
			exception.printStackTrace();
		}

		File mythicMobs = new File(MMOItems.plugin.getDataFolder() + "/dynamic/mythic-mobs-abilities");
		if (!mythicMobs.exists() && !mythicMobs.mkdirs())
			MMOItems.plugin.getLogger().warning("Failed DIR generation!");

		// Load MythicMobs addon skills
		if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
			int count = 0;
			for (File file : mythicMobs.listFiles())
				try {
					registerAbility(new MythicMobsAbility(file.getName().substring(0, file.getName().length() - 4), YamlConfiguration.loadConfiguration(file)));
					count++;
				} catch (IllegalArgumentException exception) {
					MMOItems.plugin.getLogger().log(Level.WARNING, "Could not load ability from " + file.getName() + ": " + exception.getMessage());
				}

			if (count > 0)
				MMOItems.plugin.getLogger().log(Level.INFO, "Loaded " + count + " extra MythicMobs abilities");
		}

		// Finally disable ability registration
		registration = false;
	}
}
