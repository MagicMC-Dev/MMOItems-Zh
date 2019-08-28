package net.Indyuce.mmoitems.comp;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import n3kas.ae.api.EnchantApplyEvent;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.NBTItem;

public class AdvancedEnchantmentsHook implements Listener {
	@EventHandler
	public void a(EnchantApplyEvent event) {
		NBTItem item = MMOItems.plugin.getNMS().getNBTItem(event.getItem());
		if (item.getType() != null && item.getBoolean("MMOITEMS_DISABLE_ADVANCED_ENCHANTS"))
			event.setCancelled(true);
	}
}
