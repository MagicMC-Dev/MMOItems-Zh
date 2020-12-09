package net.Indyuce.mmoitems.listener;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.util.ItemModInstance;
import net.mmogroup.mmolib.api.item.NBTItem;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class ItemListener implements Listener {
	@EventHandler(ignoreCancelled = true)
	private void itemPickup(EntityPickupItemEvent e) {
		if (!e.getEntity().getType().equals(EntityType.PLAYER)) return;

		ItemStack newItem = modifyItem(e.getItem().getItemStack(), (Player) e.getEntity(), "pickup");
		if (newItem != null) e.getItem().setItemStack(newItem);
	}

	@EventHandler(ignoreCancelled = true)
	private void itemCraft(CraftItemEvent e) {
		if(!(e.getWhoClicked() instanceof Player)) return;
		ItemStack newItem = modifyItem(e.getCurrentItem(), (Player) e.getWhoClicked(), "craft");
		if (newItem != null) e.setCurrentItem(newItem);
	}

	@EventHandler(ignoreCancelled = true)
	private void inventoryMove(InventoryClickEvent e) {
		if (e.getInventory().getType() != InventoryType.CRAFTING || !(e.getWhoClicked() instanceof Player)) return;
		ItemStack newItem = modifyItem(e.getCurrentItem(), (Player) e.getWhoClicked(), "click");
		if (newItem != null) e.setCurrentItem(newItem);
	}

	@EventHandler(ignoreCancelled = true)
	private void dropItem(PlayerDropItemEvent event) {
		NBTItem nbt = NBTItem.get(event.getItemDrop().getItemStack());
		if (!MMOItems.plugin.getConfig().getBoolean("soulbound.can-drop") && nbt.hasTag("MMOITEMS_SOULBOUND"))
			event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void playerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		ItemStack newItem = modifyItem(player.getEquipment().getHelmet(), player, "join");
		if(newItem != null) player.getEquipment().setHelmet(newItem);
		newItem = modifyItem(player.getEquipment().getChestplate(), player, "join");
		if(newItem != null) player.getEquipment().setChestplate(newItem);
		newItem = modifyItem(player.getEquipment().getLeggings(), player, "join");
		if(newItem != null) player.getEquipment().setLeggings(newItem);
		newItem = modifyItem(player.getEquipment().getBoots(), player, "join");
		if(newItem != null) player.getEquipment().setBoots(newItem);

		for (int j = 0; j < 9; j++) {
			newItem = modifyItem(player.getInventory().getItem(j), player, "join");
			if (newItem != null) player.getInventory().setItem(j, newItem);
		}

		newItem = modifyItem(player.getEquipment().getItemInOffHand(), player, "join");
		if(newItem != null) player.getEquipment().setItemInOffHand(newItem);
	}

	private ItemStack modifyItem(ItemStack stack, Player player, String type) {
		NBTItem nbt = NBTItem.get(stack);
		if (!nbt.hasType()) return null;
		ItemModInstance mod = new ItemModInstance(nbt);
		if (shouldUpdate(nbt, type))
			mod.reforge(MMOItems.plugin.getLanguage().rerollOnItemUpdate ? player : null);
		if (shouldSoulbind(nbt, type)) mod.applySoulbound(player);
		return mod.hasChanges() ? mod.toStack() : null;
	}

	/* Checks whether or not an item should be automatically soulbound */
	private boolean shouldSoulbind(NBTItem nbt, String type) {
		return nbt.getBoolean("MMOITEMS_AUTO_SOULBIND") && !nbt.hasTag("MMOITEMS_SOULBOUND")
				&& !MMOItems.plugin.getConfig().getBoolean("soulbound.auto-bind.disable-on." + type);
	}

	/* Whether or not data should be kept when updating an item to latest revision. */
	private boolean shouldUpdate(NBTItem nbt, String type) {
		if(!MMOItems.plugin.getTemplates().hasTemplate(nbt)) return false;

		return !MMOItems.plugin.getConfig().getBoolean("item-revision.disable-on." + type) &&
				((MMOItems.plugin.getTemplates().getTemplate(nbt).getRevisionId() > (nbt.hasTag("MMOITEMS_REVISION_ID")
				? nbt.getInteger("MMOITEMS_REVISION_ID") : 1)) || (MMOItems.INTERNAL_REVISION_ID >
				(nbt.hasTag("MMOITEMS_INTERNAL_REVISION_ID") ? nbt.getInteger("MMOITEMS_INTERNAL_REVISION_ID") : 1)) );
	}
}
