package net.Indyuce.mmoitems.api.event.item;

import java.util.List;

import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.event.PlayerDataEvent;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import io.lumine.mythic.lib.api.item.NBTItem;

public class DeconstructItemEvent extends PlayerDataEvent {
	private static final HandlerList handlers = new HandlerList();

	private final VolatileMMOItem consumable;
	private final NBTItem deconstructed;
	private final List<ItemStack> loot;

	/**
	 * Called when a player deconstructs an item using a consumable
	 * 
	 * @param playerData
	 *            Player deconstructing the item
	 * @param consumable
	 *            Consumable used to deconstruct the item
	 * @param deconstructed
	 *            Item being deconstructed
	 * @param loot
	 *            Items which will be given to the player if the item is
	 *            successfully deconstructed
	 */
	public DeconstructItemEvent(PlayerData playerData, VolatileMMOItem consumable, NBTItem deconstructed, List<ItemStack> loot) {
		super(playerData);

		this.consumable = consumable;
		this.deconstructed = deconstructed;
		this.loot = loot;
	}

	public VolatileMMOItem getConsumable() {
		return consumable;
	}

	public NBTItem getDeconstructedItem() {
		return deconstructed;
	}

	public List<ItemStack> getLoot() {
		return loot;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
