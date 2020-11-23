package net.Indyuce.mmoitems.stat.type;

import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.mmogroup.mmolib.api.item.NBTItem;

public interface ItemRestriction {

	/*
	 * conditional stats are used to let extra plugins implement extra item
	 * restrictions to MMOItems easily. if the method returns false, the item
	 * cannot be used by the player.
	 */
	boolean canUse(RPGPlayer player, NBTItem item, boolean message);
}
