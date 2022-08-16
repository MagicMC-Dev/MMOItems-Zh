package net.Indyuce.mmoitems.api.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.player.PlayerEvent;

import net.Indyuce.mmoitems.api.player.PlayerData;

public abstract class PlayerDataEvent extends PlayerEvent implements Cancellable {
	private final PlayerData playerData;

	private boolean cancelled;

	public PlayerDataEvent(PlayerData playerData) {
		super(playerData.getPlayer());

		this.playerData = playerData;
	}

	public PlayerData getPlayerData() {
		return playerData;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean value) {
		cancelled = value;
	}
}
