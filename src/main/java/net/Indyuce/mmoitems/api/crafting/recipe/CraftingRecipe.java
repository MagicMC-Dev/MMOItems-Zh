package net.Indyuce.mmoitems.api.crafting.recipe;

import io.lumine.mythic.lib.api.util.SmartGive;
import net.Indyuce.mmoitems.api.crafting.ConfigMMOItem;
import net.Indyuce.mmoitems.api.crafting.CraftingStation;
import net.Indyuce.mmoitems.api.crafting.CraftingStatus.CraftingQueue;
import net.Indyuce.mmoitems.api.crafting.ingredient.inventory.IngredientInventory;
import net.Indyuce.mmoitems.api.event.PlayerUseCraftingStationEvent;
import net.Indyuce.mmoitems.api.item.util.ConfigItems;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class CraftingRecipe extends Recipe {
	private final ConfigMMOItem output;

	/*
	 * There can't be any crafting time for upgrading recipes since there is no
	 * way to save an MMOItem in the config file TODO save as ItemStack
	 */
	private final double craftingTime;

	public CraftingRecipe(ConfigurationSection config) {
		super(config);

		craftingTime = config.getDouble("crafting-time");

		// load recipe output
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
	public boolean whenUsed(PlayerData data, IngredientInventory inv, CheckedRecipe recipe, CraftingStation station) {
		if (!data.isOnline())
			return false;


		/*
		 * If the recipe is instant, take the ingredients off
		 * and directly add the output to the player's inventory
		 */
		if (isInstant()) {

			ItemStack result = hasOption(RecipeOption.OUTPUT_ITEM) ? getOutput().generate(data.getRPG()) : null;
			PlayerUseCraftingStationEvent event = new PlayerUseCraftingStationEvent(data, station, recipe, result);
			Bukkit.getPluginManager().callEvent(event);
			if (event.isCancelled())
				return false;

			if (result != null)
				new SmartGive(data.getPlayer()).give(result);
			recipe.getRecipe().getTriggers().forEach(trigger -> trigger.whenCrafting(data));

			// Play sound
			if (!hasOption(RecipeOption.SILENT_CRAFT))
				data.getPlayer().playSound(data.getPlayer().getLocation(), station.getSound(), 1, 1);

			// Recipe was successfully used
			return true;

			/*
			 * If the recipe is not instant, add the item to the crafting queue
			 */
		} else {

			PlayerUseCraftingStationEvent called = new PlayerUseCraftingStationEvent(data, station, recipe);
			Bukkit.getPluginManager().callEvent(called);
			if (called.isCancelled())
				return false;

			// Play sound
			if (!hasOption(RecipeOption.SILENT_CRAFT))
				data.getPlayer().playSound(data.getPlayer().getLocation(), station.getSound(), 1, 1);

			data.getCrafting().getQueue(station).add(this);

			// Recipe was successfully used
			return true;
		}
	}

	@Override
	public boolean canUse(PlayerData data, IngredientInventory inv, CheckedRecipe recipe, CraftingStation station) {
		if (isInstant())
			return true;

		CraftingQueue queue = data.getCrafting().getQueue(station);
		if (queue.isFull(station)) {
			if (!data.isOnline())
				return false;

			Message.CRAFTING_QUEUE_FULL.format(ChatColor.RED).send(data.getPlayer());
			data.getPlayer().playSound(data.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
			return false;
		}
		return true;
	}

	@Override
	public ItemStack display(CheckedRecipe recipe) {
		return ConfigItems.CRAFTING_RECIPE_DISPLAY.newBuilder(recipe).build();
	}

	@Override
	public CheckedRecipe evaluateRecipe(PlayerData data, IngredientInventory inv) {
		return new CheckedRecipe(this, data, inv);
	}
}
