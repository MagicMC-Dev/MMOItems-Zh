package net.Indyuce.mmoitems.api.event.crafting;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import net.Indyuce.mmoitems.api.crafting.ingredient.Ingredient;

public class IngredientLoadEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	private final String format;
	private final String[] args;

	private Ingredient ingredient;

	/*
	 * based on mythic mobs registration API, this event is called whenever a
	 * ingredient is loaded. if the event doesn't return anything then the
	 * ingredient could not be loaded.
	 */
	public IngredientLoadEvent(String format, String[] args) {
		this.format = format;
		this.args = args;
	}

	public void register(Ingredient ingredient) {
		this.ingredient = ingredient;
	}

	public Ingredient getIngredient() {
		return ingredient;
	}

	public String getFormat() {
		return format;
	}

	public String[] getArguments() {
		return args;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
