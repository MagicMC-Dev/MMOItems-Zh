package net.Indyuce.mmoitems.api.drop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;

public class DropTable {
	private static final Random random = new Random();

	private List<String> subtablesList = new ArrayList<>();
	private Map<String, Subtable> subtables = new HashMap<>();

	public DropTable(ConfigurationSection section) {
		for (String key : section.getKeys(false)) {

			ConfigurationSection subtable = section.getConfigurationSection(key);
			if (!subtable.contains("coef")) {
				MMOItems.plugin.getLogger().warning("Couldn't read sub-table " + key + ": it is missing a sub-table coefficient.");
				continue;
			}
			if (!subtable.contains("items")) {
				MMOItems.plugin.getLogger().warning("Couldn't read sub-table " + key + ": it is missing sub-table items.");
				continue;
			}

			// add subtable to list & then to map
			for (int j = 0; j < section.getInt(key + ".coef"); j++)
				subtablesList.add(key);
			subtables.put(key, new Subtable(subtable));
		}
	}

	public String getRandomSubtable() {
		return subtablesList.isEmpty() ? null : subtablesList.get(random.nextInt(subtablesList.size()));
	}

	public List<ItemStack> read(boolean silkTouch) {
		List<ItemStack> dropped = new ArrayList<>();

		String randomSubtable = getRandomSubtable();
		if (randomSubtable == null)
			return dropped;

		for (DropItem dropItem : getSubtable(randomSubtable).getDropItems(silkTouch))
			if (dropItem.isDropped()) {
				ItemStack drop = dropItem.getItem();
				if (drop == null)
					MMOItems.plugin.getLogger().log(Level.WARNING, "Couldn't read the subtable item " + dropItem.getId());
				else
					dropped.add(drop);
			}

		return dropped;
	}

	public Collection<Subtable> getSubtables() {
		return subtables.values();
	}

	public Subtable getSubtable(String key) {
		return subtables.get(key);
	}

	public class Subtable {
		private List<DropItem> items = new ArrayList<>();

		/*
		 * options to prevent players from abusing drop tables.
		 */
		private boolean disableSilkTouch;

		public Subtable(ConfigurationSection subtable) {
			for (String typeFormat : subtable.getConfigurationSection("items").getKeys(false)) {
				Type type = null;
				try {
					type = MMOItems.plugin.getTypes().get(typeFormat.toUpperCase().replace("-", "_"));
				} catch (Exception e) {
					MMOItems.plugin.getLogger().warning("Couldn't read subtable " + subtable.getName() + ". " + typeFormat.toUpperCase().replace("-", "_") + " is not a valid item type.");
					continue;
				}

				for (String id : subtable.getConfigurationSection("items." + typeFormat).getKeys(false)) 
					try {
						items.add(new DropItem(type, id, subtable.getString("items." + typeFormat + "." + id)));
					} catch (Exception e) {
						MMOItems.plugin.getLogger().warning("Couldn't read subtable item " + subtable.getName() + "." + type.getId() + "." + id + ": wrong format.");
					}
			}

			disableSilkTouch = subtable.getBoolean("disable-silk-touch");
		}

		public List<DropItem> getDropItems(boolean silkTouch) {
			return silkTouch && disableSilkTouch ? new ArrayList<>() : items;
		}
	}
}
