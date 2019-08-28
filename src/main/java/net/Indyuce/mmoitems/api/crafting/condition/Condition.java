package net.Indyuce.mmoitems.api.crafting.condition;

import net.Indyuce.mmoitems.api.crafting.ConditionalDisplay;
import net.Indyuce.mmoitems.api.player.PlayerData;

public abstract class Condition {
	private final String id;
	private ConditionalDisplay display;

	public Condition(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public ConditionalDisplay getDisplay() {
		return display;
	}

	public void setDisplay(ConditionalDisplay display) {
		this.display = display;
	}

	public boolean displays() {
		return display != null;
	}

	public abstract Condition load(String[] args);

	public abstract boolean isMet(PlayerData data);

	public abstract String formatDisplay(String string);
	
	public abstract void whenCrafting(PlayerData data);

	public ConditionInfo newConditionInfo(PlayerData data) {
		return new ConditionInfo(this, isMet(data));
	}

	public class ConditionInfo {
		private final Condition condition;
		private final boolean met;

		public ConditionInfo(Condition condition, boolean met) {
			this.condition = condition;
			this.met = met;
		}

		public boolean isMet() {
			return met;
		}

		public Condition getCondition() {
			return condition;
		}
	}
}
