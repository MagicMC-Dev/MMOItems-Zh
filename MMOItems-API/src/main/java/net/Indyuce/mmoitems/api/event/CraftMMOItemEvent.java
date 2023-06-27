package net.Indyuce.mmoitems.api.event;

import net.Indyuce.mmoitems.api.player.PlayerData;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

/**
 * @deprecated Not used anymore
 */
@Deprecated
public class CraftMMOItemEvent extends PlayerDataEvent {
	private static final HandlerList handlers = new HandlerList();

	private ItemStack result;

	public CraftMMOItemEvent(PlayerData playerData, ItemStack result) {
		super(playerData);

		this.result = result;
	}

	public ItemStack getResult() {
		return result;
	}

	public void setResult(ItemStack result) {
		this.result = result;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
