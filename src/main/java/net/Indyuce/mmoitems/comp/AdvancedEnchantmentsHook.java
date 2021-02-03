package net.Indyuce.mmoitems.comp;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import n3kas.ae.api.EnchantApplyEvent;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;

public class AdvancedEnchantmentsHook implements Listener {
	@EventHandler
	public void a(EnchantApplyEvent event) {
		NBTItem item = MythicLib.plugin.getVersion().getWrapper().getNBTItem(event.getItem());
		if (item.getType() != null && item.getBoolean("MMOITEMS_DISABLE_ADVANCED_ENCHANTS"))
			event.setCancelled(true);
	}
}
