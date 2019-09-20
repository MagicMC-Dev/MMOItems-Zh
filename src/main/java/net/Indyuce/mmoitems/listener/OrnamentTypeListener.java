package net.Indyuce.mmoitems.listener;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

import net.Indyuce.mmoitems.api.Type.EquipmentSlot;
import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.api.player.PlayerData;

public class OrnamentTypeListener implements Listener {
	@EventHandler(ignoreCancelled = true)
    public void a(EntityPickupItemEvent event) {
    	if(event.getEntityType().equals(EntityType.PLAYER)) {
    		NBTItem nbt = NBTItem.get(event.getItem().getItemStack());
    		if(!nbt.hasType()) return;
    		
    		if(nbt.getType().getEquipmentType() == EquipmentSlot.ANY)
    			PlayerData.get((Player) event.getEntity()).updateInventory();
    	}
    }

	@EventHandler(ignoreCancelled = true)
    public void b(PlayerDropItemEvent event) {
		NBTItem nbt = NBTItem.get(event.getItemDrop().getItemStack());
		if(!nbt.hasType()) return;
		
		if(nbt.getType().getEquipmentType() == EquipmentSlot.ANY)
			PlayerData.get(event.getPlayer()).updateInventory();
    }
}
