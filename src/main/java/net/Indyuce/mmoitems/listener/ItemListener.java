package net.Indyuce.mmoitems.listener;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.api.util.ItemModInstance;
import net.mmogroup.mmolib.api.item.NBTItem;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class ItemListener implements Listener {
	@EventHandler(ignoreCancelled = true)
	private void itemPickup(EntityPickupItemEvent e) {
		if (!e.getEntity().getType().equals(EntityType.PLAYER)) return;
		NBTItem nbt = NBTItem.get(e.getItem().getItemStack());
		if (!nbt.hasType()) return;
		ItemModInstance mod = new ItemModInstance(nbt);
		if (shouldUpdate(nbt, "pickup")) mod.reforge(MMOItems.plugin.getLanguage().rerollOnItemUpdate ? (Player) e.getEntity() : null);
		if (shouldSoulbind(nbt, "pickup")) mod.applySoulbound((Player) e.getEntity());

		if (mod.hasChanges()) e.getItem().setItemStack(mod.applySoulbound((Player) e.getEntity()).toStack());
	}

	@EventHandler(ignoreCancelled = true)
	private void itemCraft(CraftItemEvent e) {
		NBTItem nbt = NBTItem.get(e.getCurrentItem());
		if (!nbt.hasType()) return;
		ItemModInstance mod = new ItemModInstance(nbt);
		if (shouldSoulbind(nbt, "craft")) mod.applySoulbound((Player) e.getWhoClicked());

		if (mod.hasChanges()) e.setCurrentItem(mod.toStack());
	}

	@EventHandler(ignoreCancelled = true)
	private void inventoryMove(InventoryClickEvent e) {
		NBTItem nbt = NBTItem.get(e.getCurrentItem());
		if (!nbt.hasType()) return;
		ItemModInstance mod = new ItemModInstance(nbt);
		if (shouldUpdate(nbt, "click")) mod.reforge(MMOItems.plugin.getLanguage().rerollOnItemUpdate ? (Player) e.getWhoClicked() : null);
		if (shouldSoulbind(nbt, "click")) mod.applySoulbound((Player) e.getWhoClicked());

		if (mod.hasChanges()) e.setCurrentItem(mod.toStack());
	}

	@EventHandler(ignoreCancelled = true)
	public void dropItem(PlayerDropItemEvent event) {
		NBTItem nbt = NBTItem.get(event.getItemDrop().getItemStack());
		if (!MMOItems.plugin.getConfig().getBoolean("soulbound.can-drop") && nbt.hasTag("MMOITEMS_SOULBOUND"))
			event.setCancelled(true);
	}

	/* Checks whether or not an item should be automatically soulbound */
	private boolean shouldSoulbind(NBTItem nbt, String type) {
		return nbt.getBoolean("MMOITEMS_AUTO_SOULBIND") && !nbt.hasTag("MMOITEMS_SOULBOUND") && !MMOItems.plugin.getConfig().getBoolean("soulbound.auto-bind.disable-on." + type);
	}

	/* Whether or not data should be kept when updating an item to latest revision. */
	private boolean shouldUpdate(NBTItem nbt, String type) {
		//if()
		final int templateRevId = MMOItems.plugin.getTemplates().getTemplate(nbt).getRevisionId();
		final int itemRevId = nbt.hasTag("MMOITEMS_REVISION_ID") ? nbt.getInteger("MMOITEMS_REVISION_ID") : 1;
		System.out.println("REV ID: " + templateRevId + " | " + itemRevId);
		return templateRevId <= itemRevId;
	}
}
