package net.Indyuce.mmoitems.api.event.crafting;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import net.Indyuce.mmoitems.api.crafting.condition.Condition;

public class ConditionLoadEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	private final String format;
	private final String[] args;

	private Condition condition;

	/*
	 * based on mythic mobs registration API, this event is called whenever a
	 * condition is loaded. if the event doesn't return anything then the
	 * condition could not be loaded.
	 */
	public ConditionLoadEvent(String format, String[] args) {
		this.format = format;
		this.args = args;
	}

	public void register(Condition condition) {
		this.condition = condition;
	}

	public Condition getCondition() {
		return condition;
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
