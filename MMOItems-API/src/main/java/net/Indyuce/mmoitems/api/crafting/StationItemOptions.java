package net.Indyuce.mmoitems.api.crafting;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.item.util.ConfigItem;

public class StationItemOptions {
	private final ItemStack fill, noRecipe, noQueueItem;

	public StationItemOptions(ConfigurationSection config) {
		fill = new ConfigItem(config.getConfigurationSection("fill")).getItem();
		noRecipe = new ConfigItem(config.getConfigurationSection("no-recipe")).getItem();
		noQueueItem = new ConfigItem(config.getConfigurationSection("no-queue-item")).getItem();
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
}
