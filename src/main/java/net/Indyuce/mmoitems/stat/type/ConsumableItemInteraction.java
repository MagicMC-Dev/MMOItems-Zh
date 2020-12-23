package net.Indyuce.mmoitems.stat.type;

import org.bukkit.event.inventory.InventoryClickEvent;

import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.interaction.Consumable;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.mmogroup.mmolib.api.item.NBTItem;

/**
 * Stats which implement a consumable action like deconstructing, identifying,
 * applying a skin onto an item...
 * 
 * @author cympe
 *
 */
public interface ConsumableItemInteraction {

	/**
	 * Applies a consumable effect onto the item.
	 * 
	 * @param  event      The click event
	 * @param  playerData The player applying the consumable
	 * @param  consumable The consumable being applied with a VolatileMMOItem
	 *                    stored inside
	 * @param  target     The target item
	 * @param  targetType The item type of target item
	 * @return            True if the consumable effect was successfully applied
	 *                    (basically return true if it should be the only
	 *                    consumable effect applied).
	 */
	public boolean handleConsumableEffect(InventoryClickEvent event, PlayerData playerData, Consumable consumable, NBTItem target, Type targetType);
}
