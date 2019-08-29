package net.Indyuce.mmoitems.stat.type;

import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.api.player.RPGPlayer;

public interface Conditional {
	public boolean canUse(RPGPlayer player, NBTItem item, boolean message);
}
