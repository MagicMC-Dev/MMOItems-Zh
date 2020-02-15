package net.Indyuce.mmoitems.api.crafting.trigger;

import net.Indyuce.mmoitems.api.player.PlayerData;

public abstract class Trigger {
	private final String id;

	public Trigger(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public abstract void whenCrafting(PlayerData data);
}
