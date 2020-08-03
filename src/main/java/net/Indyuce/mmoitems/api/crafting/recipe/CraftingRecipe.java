package net.Indyuce.mmoitems.api.crafting.recipe;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.crafting.ConfigMMOItem;
import net.Indyuce.mmoitems.api.crafting.CraftingStation;
import net.Indyuce.mmoitems.api.crafting.CraftingStatus.CraftingQueue;
import net.Indyuce.mmoitems.api.crafting.IngredientInventory;
import net.Indyuce.mmoitems.api.item.plugin.ConfigItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.mmogroup.mmolib.api.util.SmartGive;

public class CraftingRecipe extends Recipe {
	private final ConfigMMOItem output;

	/*
	 * there can't be any crafting time for upgrading recipes since there is no way
	 * to save an MMOItem in the config file TODO save as ItemStack
	 */
	private final double craftingTime;

	public CraftingRecipe(ConfigurationSection config) {
		super(config);

		craftingTime = config.getDouble("crafting-time");

		/*
		 * load recipe output
		 */
		output = new ConfigMMOItem(config.getConfigurationSection("output"));
	}

	public double getCraftingTime() {
		return craftingTime;
	}

	public boolean isInstant() {
		return craftingTime <= 0;
	}

	public ConfigMMOItem getOutput() {
		return output;
	}

	@Override
	public void whenUsed(PlayerData data, IngredientInventory inv, RecipeInfo recipe, CraftingStation station) {
		/*
		 * if the recipe is an instant recipe, just take off the ingredients add
		 * directly add the ingredients to the player inventory
		 */
		if (isInstant()) {
			if (getOption(RecipeOption.OUTPUT_ITEM))
				new SmartGive(data.getPlayer()).give(getOutput().generate());
			recipe.getRecipe().getTriggers().forEach(trigger -> trigger.whenCrafting(data));
			if (!getOption(RecipeOption.SILENT_CRAFT))
				data.getPlayer().playSound(data.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
			/*
			 * if recipe not instant, add item to crafting queue, either way RELOAD
			 * inventory data and reopen inventory!
			 */
		} else
			data.getCrafting().getQueue(station).add(this);

		if (!isInstant())
			data.getPlayer().playSound(data.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
	}

	@Override
	public boolean canUse(PlayerData data, IngredientInventory inv, RecipeInfo recipe, CraftingStation station) {
		if (isInstant())
			return true;

		CraftingQueue queue = data.getCrafting().getQueue(station);
		if (queue.isFull(station)) {
			Message.CRAFTING_QUEUE_FULL.format(ChatColor.RED).send(data.getPlayer());
			data.getPlayer().playSound(data.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
			return false;
		}
		return true;
	}

	@Override
	public ItemStack display(RecipeInfo recipe) {
		return ConfigItem.CRAFTING_RECIPE_DISPLAY.newBuilder(recipe).build();
	}
}
