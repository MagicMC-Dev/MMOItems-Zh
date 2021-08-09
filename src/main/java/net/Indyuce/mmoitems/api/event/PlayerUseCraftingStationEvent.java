package net.Indyuce.mmoitems.api.event;

import net.Indyuce.mmoitems.api.crafting.CraftingStation;
import net.Indyuce.mmoitems.api.crafting.recipe.CheckedRecipe;
import net.Indyuce.mmoitems.api.crafting.recipe.CraftingRecipe;
import net.Indyuce.mmoitems.api.crafting.recipe.Recipe;
import net.Indyuce.mmoitems.api.crafting.recipe.UpgradingRecipe;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.apache.commons.lang.Validate;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

public class PlayerUseCraftingStationEvent extends PlayerDataEvent {
	private final CraftingStation station;
	private final Recipe recipe;
	private final StationAction action;

	@Nullable
	private final CheckedRecipe recipeInfo;
	@Nullable
	private final ItemStack result;

	private static final HandlerList handlers = new HandlerList();

	/**
	 * Called when interacting with a non instant recipe. The item
	 * is therefore sent to the crafting queue.
	 *
	 * @param playerData The player interacting with the crafting station
	 * @param station    The crafting station being used
	 * @param recipeInfo The recipe being used to craft the item
	 */
	public PlayerUseCraftingStationEvent(PlayerData playerData, CraftingStation station, CheckedRecipe recipeInfo) {
		this(playerData, station, recipeInfo, recipeInfo.getRecipe(), null, StationAction.INTERACT_WITH_RECIPE);
	}

	/**
	 * Called when interacting with an upgrading recipe
	 *
	 * @param playerData The player interacting with the crafting station
	 * @param station    The crafting station being used
	 * @param recipeInfo The recipe being used to craft the item
	 */
	public PlayerUseCraftingStationEvent(PlayerData playerData, CraftingStation station, UpgradingRecipe.CheckedUpgradingRecipe recipeInfo) {
		this(playerData, station, recipeInfo, recipeInfo.getRecipe(), null, StationAction.UPGRADE_RECIPE);
	}

	/**
	 * Called when interacting with an instant recipe
	 *
	 * @param playerData The player interacting with the crafting station
	 * @param station    The crafting station being used
	 * @param recipeInfo The recipe being used to craft the item
	 * @param result     The item given to the player
	 */
	public PlayerUseCraftingStationEvent(PlayerData playerData, CraftingStation station, CheckedRecipe recipeInfo, ItemStack result) {
		this(playerData, station, recipeInfo, recipeInfo.getRecipe(), result, StationAction.INSTANT_RECIPE);
	}

	/**
	 * Called when a player claims an item from the crafting queue
	 *
	 * @param playerData The player interacting with the crafting station
	 * @param station    The crafting station being used
	 * @param recipe     The recipe being used to craft the item
	 * @param result     The item given to the player
	 */
	public PlayerUseCraftingStationEvent(PlayerData playerData, CraftingStation station, Recipe recipe, ItemStack result) {
		this(playerData, station, null, recipe, result, StationAction.CRAFTING_QUEUE);
	}

	/**
	 * Called when a player removes an item from the queue
	 *
	 * @param playerData The player interacting with the crafting station
	 * @param station    The crafting station being used
	 * @param recipe     The recipe being used to craft the item
	 */
	public PlayerUseCraftingStationEvent(PlayerData playerData, CraftingStation station, Recipe recipe) {
		this(playerData, station, null, recipe, null, StationAction.CANCEL_QUEUE);
	}

	private PlayerUseCraftingStationEvent(PlayerData playerData, CraftingStation station, CheckedRecipe recipeInfo, Recipe recipe, ItemStack result, StationAction action) {
		super(playerData);

		this.station = station;
		this.recipe = recipe;
		this.recipeInfo = recipeInfo;
		this.result = result;
		this.action = action;
	}

	public CraftingStation getStation() {
		return station;
	}

	public boolean hasRecipeInfo() {
		return recipeInfo != null;
	}

	/**
	 * @return The corresponding recipe info if the current
	 * interaction is either INTERACT_WITH_RECIPE or INSTANT_RECIPE
	 */
	public CheckedRecipe getRecipeInfo() {
		Validate.notNull(action == StationAction.INSTANT_RECIPE || action == StationAction.INTERACT_WITH_RECIPE, "No recipe info is provided with " + action.name());
		return recipeInfo;
	}

	public boolean hasResult() {
		return result != null;
	}

	/**
	 * @return The result item if the current interaction
	 * is either INSTANT_RECIPE or CRAFTING_QUEUE
	 */
	public ItemStack getResult() {
		Validate.notNull(action == StationAction.INSTANT_RECIPE || action == StationAction.CRAFTING_QUEUE, "No result item is provided with " + action.name());
		return result;
	}


	public Recipe getRecipe() {
		return recipe;
	}

	public StationAction getInteraction() {
		return action;
	}

	/**
	 * @return If the recipe used is an instantaneous recipe
	 * @deprecated Check if the interaction type is INSTANT_RECIPE instead
	 */
	@Deprecated
	public boolean isInstant() {
		return recipe instanceof CraftingRecipe && ((CraftingRecipe) recipe).isInstant();
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public enum StationAction {

		/**
		 * Called when a player uses an upgrading recipe. Upgrading recipes are always instantaneous
		 */
		UPGRADE_RECIPE,

		/**
		 * Called when a player places an item in the crafting queue when the
		 * recipe is not instantaneous
		 */
		INTERACT_WITH_RECIPE,

		/**
		 * Called when the player directly gets the item via an instant recipe
		 */
		INSTANT_RECIPE,

		/**
		 * Called when a player claims the item from the crafting queue due to a
		 * non instant crafting recipe
		 */
		CRAFTING_QUEUE,

		/**
		 * Called when a player removes an item from the crafting queue
		 */
		CANCEL_QUEUE
	}
}
