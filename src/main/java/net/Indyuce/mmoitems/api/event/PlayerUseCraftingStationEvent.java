package net.Indyuce.mmoitems.api.event;

import net.Indyuce.mmoitems.api.crafting.CraftingStation;
import net.Indyuce.mmoitems.api.crafting.recipe.CraftingRecipe;
import net.Indyuce.mmoitems.api.crafting.recipe.Recipe;
import net.Indyuce.mmoitems.api.crafting.recipe.RecipeInfo;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.apache.commons.lang.Validate;
import org.bukkit.event.HandlerList;

public class PlayerUseCraftingStationEvent extends PlayerDataEvent {
	private final Recipe recipe;
	private final RecipeInfo recipeInfo;
	private final CraftingStation station;
	private final StationAction action;

	private static final HandlerList handlers = new HandlerList();

	/**
	 * Called when a player directly interacts with a recipe in the crafting
	 * station GUI. The recipe is either instant and the item is given
	 * instaneously, or the item is sent in the crafting queue
	 * 
	 * @param playerData
	 *            The player interacting with the crafting station
	 * @param station
	 *            The crafting station being used
	 * @param recipeInfo
	 *            The recipe being used to craft the item
	 */
	public PlayerUseCraftingStationEvent(PlayerData playerData, CraftingStation station, RecipeInfo recipeInfo, StationAction action) {
		this(playerData, station, recipeInfo, recipeInfo.getRecipe(), action);
	}

	/**
	 * Called when a player claims an item from the crafting queue.
	 * 
	 * @param playerData
	 *            The player interacting with the crafting station
	 * @param station
	 *            The crafting station being used
	 * @param recipe
	 *            The recipe being used to craft the item
	 */
	public PlayerUseCraftingStationEvent(PlayerData playerData, CraftingStation station, Recipe recipe, StationAction action) {
		this(playerData, station, null, recipe, action);
	}


	private PlayerUseCraftingStationEvent(PlayerData playerData, CraftingStation station, RecipeInfo recipeInfo, Recipe recipe, StationAction action) {
		super(playerData);

		this.recipeInfo = recipeInfo;
		this.recipe = recipe;
		this.station = station;
		this.action = action;
	}

	public CraftingStation getStation() {
		return station;
	}

	/**
	 * @return The corresponding recipe info IF AND ONLY IF the player is
	 *         interacting with a recipe. This method cannot be used when a
	 *         player claims an item from the crafting queue.
	 */
	public RecipeInfo getRecipeInfo() {
		Validate.notNull(recipeInfo, "No recipe info is provided when a player claims an item in the crafting queue");
		return recipeInfo;
	}

	public boolean hasRecipeInfo() {
		return recipeInfo != null;
	}

	public Recipe getRecipe() {
		return recipe;
	}

	public StationAction getInteraction() {
		return action;
	}

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
