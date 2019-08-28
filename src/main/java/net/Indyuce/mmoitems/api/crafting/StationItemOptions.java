package net.Indyuce.mmoitems.api.crafting;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmoitems.api.item.plugin.NamedItemStack;
import net.Indyuce.mmoitems.version.VersionMaterial;

public class StationItemOptions {
	private final CraftingStation station;

	private ItemStack fill, noRecipe, noQueueItem;

	private static final ItemStack defaultFill = new ItemStack(Material.AIR), defaultNoRecipe = new NamedItemStack(VersionMaterial.GRAY_STAINED_GLASS_PANE.toMaterial(), "&aNo Recipe"), defaultNoQueueItem = new NamedItemStack(VersionMaterial.GRAY_STAINED_GLASS_PANE.toMaterial(), "&aNo Item In Queue");

	public StationItemOptions(CraftingStation station, ConfigurationSection config) {
		this.station = station;

		fill = new ConfigItem(defaultFill).load("fill", config.getConfigurationSection("fill")).toItem();
		noRecipe = new ConfigItem(defaultNoRecipe).load("no-recipe", config.getConfigurationSection("no-recipe")).toItem();
		noQueueItem = new ConfigItem(defaultNoQueueItem).load("no-queue-item", config.getConfigurationSection("no-queue-item")).toItem();
	}

	public ItemStack getFill() {
		return fill;
	}

	public ItemStack getNoRecipe() {
		return noRecipe;
	}

	public ItemStack getNoQueueItem() {
		return noQueueItem;
	}

	public boolean hasFill() {
		return fill.getType() != Material.AIR;
	}

	public boolean hasNoRecipe() {
		return noRecipe.getType() != Material.AIR;
	}

	public boolean hasNoQueueItem() {
		return noQueueItem.getType() != Material.AIR;
	}

	public class ConfigItem {
		private ItemStack item, def;

		public ConfigItem(ItemStack def) {
			this.def = def;
		}

		public ConfigItem load(String key, ConfigurationSection config) {
			if (config == null || !config.contains("material")) {
				station.log("Could not load item '" + (config == null ? key : config.getName()) + "'");
				return this;
			}

			try {
				item = new ItemStack(Material.valueOf(config.getString("material").toUpperCase().replace(" ", "_").replace("-", "_")));
			} catch (IllegalArgumentException exception) {
				station.log("Could not load item option " + config.getName());
				return this;
			}

			/*
			 * if item is air, the item meta cannot generate. directly return
			 * the config item.
			 */
			if (item.getType() == Material.AIR)
				return this;

			ItemMeta meta = item.getItemMeta();

			if (config.contains("name"))
				meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString("name")));

			if (config.contains("lore")) {
				List<String> lore = new ArrayList<>();
				config.getStringList("lore").forEach(str -> lore.add(ChatColor.translateAlternateColorCodes('&', str)));
				meta.setLore(lore);
			}

			item.setItemMeta(meta);
			return this;
		}

		public boolean isValid() {
			return item != null;
		}

		public ItemStack toItem() {
			return isValid() ? item : def;
		}
	}
}
