package net.Indyuce.mmoitems.listener;

import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ReforgeOptions;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.util.MMOItemReforger;
import io.lumine.mythic.lib.api.item.NBTItem;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

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

	@Nullable private ItemStack modifyItem(@NotNull ItemStack stack, @NotNull Player player, @NotNull String type) {

		// Get the item
		NBTItem nbt = NBTItem.get(stack);

		// Not a MMOItem? not our problem.
		if (!nbt.hasType()) { return null; }

		// ??
		MMOItemReforger mod = new MMOItemReforger(nbt);

		// Should this item be updated (via the updater)
		if (shouldUpdate(nbt, type)) {

			// Reforge vs Update bro
			if (MMOItems.plugin.getLanguage().rerollOnItemUpdate) {
				mod.reforge(player, MMOItems.plugin.getLanguage().revisionOptions); } else {
				mod.update(player, MMOItems.plugin.getLanguage().revisionOptions); }
		}

		// Should this item be soulbound?
		if (shouldSoulbind(nbt, type)) { mod.applySoulbound(player); }

		// L
		if (!mod.hasChanges()) { return null; }

		// Perform all operations (including extracting lost gems)
		ItemStack ret = mod.toStack();

		// Give the gems to the player
		if (ReforgeOptions.dropRestoredGems) {
			//XTC//MMOItems.log("\u00a7a *\u00a7e*\u00a77 Dropping lost gemstones (\u00a73" + mod.getDestroyedGems().size() + "\u00a78)");

			// Get Items
			ArrayList<ItemStack> items = new ArrayList<>();

			// Build and drop every lost gemstone
			for (MMOItem item : mod.getDestroyedGems()) {

				// Build
				ItemStack built = item.newBuilder().build();
				//XTC//MMOItems.log("\u00a7e   *\u00a77 Saved " + SilentNumbers.getItemName(built));

				// Include
				items.add(built); }

			// Drop those gems
			for (ItemStack drop : player.getInventory().addItem(
					items.toArray(new ItemStack[0])).values()) {

				// Not air right
				if (SilentNumbers.isAir(drop)) { continue; }

				// Drop to the world
				player.getWorld().dropItem(player.getLocation(), drop); } }

		// Return the modified version
		return ret;
	}

	/* Checks whether or not an item should be automatically soulbound */
	private boolean shouldSoulbind(NBTItem nbt, String type) {
		return nbt.getBoolean("MMOITEMS_AUTO_SOULBIND") && !nbt.hasTag("MMOITEMS_SOULBOUND")
				&& !MMOItems.plugin.getConfig().getBoolean("soulbound.auto-bind.disable-on." + type);
	}

	/* Whether or not data should be kept when updating an item to latest revision. */
	private boolean shouldUpdate(NBTItem nbt, String type) {
		if(!MMOItems.plugin.getTemplates().hasTemplate(nbt)) return false;

		return
				// It must not be disabled for this item
				!MMOItems.plugin.getConfig().getBoolean("item-revision.disable-on." + type) &&


				// Either the normal revision ID or the internal revision IDs are out of date.
				(
						// Is the template's revision ID greater than the one currently in the item?
						(MMOItems.plugin.getTemplates().getTemplate(nbt).getRevisionId() >

								// The one 'currently in the item' is the value of the stat, or 1 if missing.
								(nbt.hasTag(ItemStats.REVISION_ID.getNBTPath()) ? nbt.getInteger(ItemStats.REVISION_ID.getNBTPath()) : 1)) ||

						// Or the MMOItems internal revision ID itself
						(MMOItems.INTERNAL_REVISION_ID >

								// Same thing: either the value of the stat or 1 if missing.
								(nbt.hasTag(ItemStats.INTERNAL_REVISION_ID.getNBTPath()) ? nbt.getInteger(ItemStats.INTERNAL_REVISION_ID.getNBTPath()) : 1))
				);
	}
}
