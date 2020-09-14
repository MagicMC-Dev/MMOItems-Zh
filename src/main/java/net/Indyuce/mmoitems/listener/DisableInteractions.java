package net.Indyuce.mmoitems.listener;

import net.Indyuce.mmoitems.MMOItems;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.NBTItem;
import org.bukkit.Keyed;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class DisableInteractions implements Listener {

	// anvils
	@EventHandler
	public void a(InventoryClickEvent event) {
		Inventory inv = event.getClickedInventory();
		if (inv == null || inv.getType() != InventoryType.ANVIL || event.getSlot() != 2)
			return;

		NBTItem item = NBTItem.get(event.getCurrentItem());
		if (item.hasType()
				&& (MMOItems.plugin.getConfig().getBoolean("disable-interactions.repair") || item.getBoolean("MMOITEMS_DISABLE_REPAIRING")))
			event.setCancelled(true);
	}

	// grindstone
	@EventHandler
	public void b(InventoryClickEvent event) {
		if (MMOLib.plugin.getVersion().isBelowOrEqual(1, 13))
			return;

		Inventory inv = event.getClickedInventory();
		if (inv == null || inv.getType() != InventoryType.GRINDSTONE || event.getSlot() != 2)
			return;

		NBTItem item = NBTItem.get(event.getCurrentItem());
		if (item.hasType()
				&& (MMOItems.plugin.getConfig().getBoolean("disable-interactions.repair") || item.getBoolean("MMOITEMS_DISABLE_REPAIRING")))
			event.setCancelled(true);
	}

	// smithing table
	@EventHandler
	public void c(InventoryClickEvent event) {
		if (MMOLib.plugin.getVersion().isBelowOrEqual(1, 15))
			return;

		Inventory inv = event.getClickedInventory();
		if (inv == null || inv.getType() != InventoryType.SMITHING || event.getSlot() != 2)
			return;

		NBTItem item = NBTItem.get(event.getCurrentItem());
		if (item.hasType() && (MMOItems.plugin.getConfig().getBoolean("disable-interactions.smith") || item.getBoolean("MMOITEMS_DISABLE_SMITHING")))
			event.setCancelled(true);
	}

	// enchanting tables
	@EventHandler
	public void d(EnchantItemEvent event) {
		NBTItem item = NBTItem.get(event.getItem());
		if (item.hasType()
				&& (MMOItems.plugin.getConfig().getBoolean("disable-interactions.enchant") || item.getBoolean("MMOITEMS_DISABLE_ENCHANTING")))
			event.setCancelled(true);
	}

	// smelting
	@EventHandler
	public void e(FurnaceSmeltEvent event) {
		NBTItem item = NBTItem.get(event.getSource());
		if (item.hasType() && (MMOItems.plugin.getConfig().getBoolean("disable-interactions.smelt") || item.getBoolean("MMOITEMS_DISABLE_SMELTING")))
			event.setCancelled(true);
	}

	// interaction
	@EventHandler
	public void f(PlayerInteractEvent event) {
		if (!event.hasItem())
			return;

		NBTItem item = NBTItem.get(event.getItem());
		if (item.getBoolean("MMOITEMS_DISABLE_INTERACTION"))
			event.setCancelled(true);
	}

	// interaction (entity)
	@EventHandler
	public void g(PlayerInteractEntityEvent event) {
		if (event.getRightClicked() instanceof ArmorStand)
			return;

		NBTItem item = NBTItem.get(event.getHand() == EquipmentSlot.OFF_HAND ? event.getPlayer().getInventory().getItemInOffHand()
				: event.getPlayer().getInventory().getItemInMainHand());
		if (item.getBoolean("MMOITEMS_DISABLE_INTERACTION"))
			event.setCancelled(true);
	}

	// interaction (consume)
	@EventHandler
	public void h(PlayerItemConsumeEvent event) {
		NBTItem item = NBTItem.get(event.getItem());
		if (item.getBoolean("MMOITEMS_DISABLE_INTERACTION"))
			event.setCancelled(true);
	}

	// workbench
	@EventHandler
	public void i(CraftItemEvent event) {
		if (event.getRecipe() instanceof Keyed)
			if (((Keyed) event.getRecipe()).getKey().getNamespace().equalsIgnoreCase("mmoitems"))
				return;

		boolean disableCrafting = MMOItems.plugin.getConfig().getBoolean("disable-interactions.craft");
		for (ItemStack item : event.getInventory().getMatrix()) {
			NBTItem nbtItem = NBTItem.get(item);
			if (nbtItem.getType() != null)
				if (disableCrafting || nbtItem.getBoolean("MMOITEMS_DISABLE_CRAFTING"))
					event.setCancelled(true);
		}

		if (MMOItems.plugin.getConfig().getStringList("disable-vanilla-recipes").contains(event.getCurrentItem().getType().name()))
			event.setCancelled(true);
	}

	// preventing the player from shooting the arrow
	@EventHandler
	public void j(EntityShootBowEvent event) {
		if (!(event.getEntity() instanceof Player))
			return;

		Player player = (Player) event.getEntity();
		int arrowSlot = firstArrow(player);
		if (arrowSlot < 0)
			return;

		ItemStack stack = player.getInventory().getItem(arrowSlot);
		if (stack == null)
			return;

		NBTItem arrow = NBTItem.get(stack);
		if (arrow.hasType() && MMOItems.plugin.getConfig().getBoolean("disable-interactions.arrow-shooting")
				|| arrow.getBoolean("MMOITEMS_DISABLE_ARROW_SHOOTING"))
			event.setCancelled(true);
	}

	private int firstArrow(Player player) {

		// check offhand first
		if (player.getInventory().getItemInOffHand() != null && player.getInventory().getItemInOffHand().getType().name().contains("ARROW"))
			return 40;

		// check for every slot
		ItemStack[] storage = player.getInventory().getStorageContents();
		for (int j = 0; j < storage.length; j++) {
			ItemStack item = storage[j];
			if (item != null && item.getType().name().contains("ARROW"))
				return j;
		}
		return -1;
	}
}
