package net.Indyuce.mmoitems.comp;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.gmail.nossr50.events.skills.repair.McMMOPlayerRepairCheckEvent;

import io.lumine.mythic.lib.api.item.NBTItem;

/**
 * The McMMOHook class is only instantiated if McMMO is being used by MMOItems
 * as a RPGCore plugin however there are features which should enable even if
 * McMMO is not detected as a RPG Core plugin if other RPG plugins are used at
 * the same time.
 * 
 * @author cympe
 *
 */
public class McMMONonRPGHook implements Listener {

	@EventHandler(ignoreCancelled = true)
	public void handleNoMcMMORepair(McMMOPlayerRepairCheckEvent event) {
		NBTItem nbt = NBTItem.get(event.getRepairedObject());
		if (nbt.hasType() && nbt.getBoolean("MMOITEMS_DISABLE_MCMMO_REPAIR"))
			event.setCancelled(true);
	}
}
