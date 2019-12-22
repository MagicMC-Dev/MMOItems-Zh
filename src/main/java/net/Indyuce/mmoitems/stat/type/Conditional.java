package net.Indyuce.mmoitems.stat.type;

import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.mmogroup.mmolib.api.item.NBTItem;

public interface Conditional {
	public boolean canUse(RPGPlayer player, NBTItem item, boolean message);
}
