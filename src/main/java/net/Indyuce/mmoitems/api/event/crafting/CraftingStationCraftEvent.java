package net.Indyuce.mmoitems.api.event.crafting;

import org.bukkit.event.HandlerList;

import net.Indyuce.mmoitems.api.crafting.CraftingStation;
import net.Indyuce.mmoitems.api.crafting.recipe.Recipe;
import net.Indyuce.mmoitems.api.event.PlayerDataEvent;
import net.Indyuce.mmoitems.api.player.PlayerData;

public class CraftingStationCraftEvent extends PlayerDataEvent {
	private final Recipe recipe;
	private final CraftingStation station;
	private final boolean instant;

	private static final HandlerList handlers = new HandlerList();

	public CraftingStationCraftEvent(PlayerData playerData, CraftingStation station, Recipe recipe, boolean instant) {
		super(playerData);
		
		this.recipe = recipe;
		this.station = station;
		this.instant = instant;
	}

	public CraftingStation getStation() {
		return station;
	}
	
	public Recipe getRecipe() {
		return recipe;
	}
	
	public boolean isInstant() {
		return instant;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
