package net.Indyuce.mmoitems.api.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;

import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.stat.data.AbilityData;

@Deprecated
public class AbilityUseEvent extends PlayerDataEvent {
	private static final HandlerList handlers = new HandlerList();

	private final AbilityData ability;
	private final LivingEntity target;

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

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
