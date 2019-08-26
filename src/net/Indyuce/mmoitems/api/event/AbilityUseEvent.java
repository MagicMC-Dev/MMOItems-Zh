package net.Indyuce.mmoitems.api.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;

import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.stat.data.AbilityData;

public class AbilityUseEvent extends PlayerDataEvent {
	private static final HandlerList handlers = new HandlerList();

	private final AbilityData ability;
	private final LivingEntity target;

	private boolean cancelled;

	public AbilityUseEvent(PlayerData playerData, AbilityData ability) {
		this(playerData, ability, null);
	}

	public AbilityUseEvent(PlayerData playerData, AbilityData ability, LivingEntity target) {
		super(playerData);
		this.ability = ability;
		this.target = target;
	}

	public AbilityData getAbility() {
		return ability;
	}

	public LivingEntity getTarget() {
		return target;
	}

	@Override
	public void setCancelled(boolean bool) {
		cancelled = bool;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
