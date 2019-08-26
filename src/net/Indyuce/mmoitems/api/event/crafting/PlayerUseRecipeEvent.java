package net.Indyuce.mmoitems.api.event.crafting;

import org.bukkit.event.HandlerList;

import net.Indyuce.mmoitems.api.crafting.CraftingStation;
import net.Indyuce.mmoitems.api.crafting.recipe.Recipe;
import net.Indyuce.mmoitems.api.crafting.recipe.RecipeInfo;
import net.Indyuce.mmoitems.api.event.PlayerDataEvent;
import net.Indyuce.mmoitems.api.player.PlayerData;

public class PlayerUseRecipeEvent extends PlayerDataEvent {
	private final RecipeInfo info;
	private final CraftingStation station;

	private static final HandlerList handlers = new HandlerList();

	public PlayerUseRecipeEvent(PlayerData playerData, CraftingStation station, RecipeInfo info) {
		super(playerData);
		
		this.info = info;
		this.station = station;
	}

	public CraftingStation getStation() {
		return station;
	}
	
	public RecipeInfo getRecipeInfo() {
		return info;
	}
	
	public Recipe getRecipe() {
		return info.getRecipe();
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
