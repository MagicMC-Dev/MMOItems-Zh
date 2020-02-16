package net.Indyuce.mmoitems.api.crafting.condition;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.crafting.ConditionalDisplay;
import net.Indyuce.mmoitems.api.player.PlayerData;

public abstract class Condition {
	private final String id;

	public Condition(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	/*
	 * shortcut to RecipeManager map lookup, may throw a stream lookup error if
	 * the condition has not been registered.
	 */
	public ConditionalDisplay getDisplay() {
		return MMOItems.plugin.getCrafting().getConditions().stream().filter(type -> type.getId().equals(id)).findAny().get().getDisplay();
	}

	public abstract boolean isMet(PlayerData data);

	public abstract String formatDisplay(String string);

	public abstract void whenCrafting(PlayerData data);

	public CheckedCondition newConditionInfo(PlayerData data) {
		return new CheckedCondition(this, isMet(data));
	}

	public class CheckedCondition {
		private final Condition condition;
		private final boolean met;

		public CheckedCondition(Condition condition, boolean met) {
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
