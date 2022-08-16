package net.Indyuce.mmoitems.stat.type;

import net.Indyuce.mmoitems.api.player.RPGPlayer;
import io.lumine.mythic.lib.api.item.NBTItem;

/**
 * Stats which implement an item restriction. They are automatically collected
 * in a list when registered in the StatManager. Lets other plugins implement
 * stats which rely on item restrictions without having to use Bukkit events!
 * 
 * @author cympe
 * 
 */
public interface ItemRestriction {

	/**
	 * @param  player  Player trying to use an item
	 * @param  item    The item being checked
	 * @param  message Difference between an active and a passive check: if the
	 *                 check is active (message boolean set to true), the plugin
	 *                 should send a message to the player if he can't use the
	 *                 item for eg when he tries to equip the item. If the check
	 *                 is passive, no message needs to be sent (when the plugin
	 *                 internally needs some similar check)
	 * @return         False if the item cannot be used
	 */
	boolean canUse(RPGPlayer player, NBTItem item, boolean message);

	/**
	 * Usually, item restrictions are checked <i>when equipping</i>
	 * an item, and prevent the item being equipped if they fail that moment.
	 * <p></p>
	 * Setting this to <code>true</code> will allow items equip anyway
	 * and check with every use that the conditions for their usage
	 * are met.
	 */
	default boolean isDynamic() { return false; }
}
