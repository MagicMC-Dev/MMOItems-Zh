package net.Indyuce.mmoitems.listener;

import org.bukkit.Keyed;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.interaction.util.DurabilityItem;

public class DisableInteractions implements Listener {

	// anvils
	@EventHandler
	public void a(InventoryClickEvent event) {
		Inventory inv = event.getClickedInventory();
		if (inv == null || inv.getType() != InventoryType.ANVIL || event.getSlotType() != SlotType.RESULT)
			return;

		if(isDisabled(NBTItem.get(event.getCurrentItem()), "repair"))
			event.setCancelled(true);
		else if(inv.getItem(1) != null && isDisabled(NBTItem.get(inv.getItem(1)), "repair"))
			event.setCancelled(true);
	}

	// grindstone
	@EventHandler
	public void b(InventoryClickEvent event) {
		if (MythicLib.plugin.getVersion().isBelowOrEqual(1, 13))
			return;

		Inventory inv = event.getClickedInventory();
		if (inv == null || inv.getType() != InventoryType.GRINDSTONE || event.getSlotType() != SlotType.RESULT)
			return;

		if(isDisabled(NBTItem.get(inv.getItem(0)), "repair") || isDisabled(NBTItem.get(inv.getItem(1)), "repair"))
			event.setCancelled(true);
	}

	// smithing table
	@EventHandler
	public void c(InventoryClickEvent event) {
		if (MythicLib.plugin.getVersion().isBelowOrEqual(1, 15))
			return;

		Inventory inv = event.getClickedInventory();
		if (inv == null || inv.getType() != InventoryType.SMITHING || event.getSlotType() != SlotType.RESULT)
			return;

		if(NBTItem.get(event.getCurrentItem()).hasType())
			return;
		
		if(isDisabled(NBTItem.get(inv.getItem(0)), "smith") || isDisabled(NBTItem.get(inv.getItem(1)), "smith"))
			event.setCancelled(true);
	}

	// enchanting tables
	@EventHandler
	public void d(EnchantItemEvent event) {
		if (isDisabled(NBTItem.get(event.getItem()), "enchant"))
			event.setCancelled(true);
	}

	// smelting
	@EventHandler
	public void e(FurnaceSmeltEvent event) {
		if (isDisabled(NBTItem.get(event.getSource()), "smelt"))
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
			if (((Keyed) event.getRecipe()).getKey().getNamespace().equals("mmoitems")) {
				String craftingPerm = NBTItem.get(event.getCurrentItem()).getString("MMOITEMS_CRAFT_PERMISSION");
				if(!craftingPerm.isEmpty() && !event.getWhoClicked().hasPermission(craftingPerm)) event.setCancelled(true);
				return;
			}

		for (ItemStack item : event.getInventory().getMatrix()) {
			if(isDisabled(NBTItem.get(item), "craft")) {
				event.setCancelled(true);
				return;
			}
		}

		if (MMOItems.plugin.getConfig().getStringList("disable-vanilla-recipes").contains(event.getCurrentItem().getType().name()))
			event.setCancelled(true);
	}

	// preventing the player from shooting the arrow
	@EventHandler
	public void j(EntityShootBowEvent event) {
		if (!(event.getEntity() instanceof Player))
			return;

		DurabilityItem durItem = new DurabilityItem(((Player) event.getEntity()).getPlayer(), event.getBow());

		if (durItem.isBroken())
			event.setCancelled(true);

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
	
	private boolean isDisabled(NBTItem nbt, String type) {
		return nbt.hasType() && MMOItems.plugin.getConfig().getBoolean("disable-interactions." + type)
				|| nbt.getBoolean("MMOITEMS_DISABLE_" + type.toUpperCase().replace("-", "_") + "ING");
	}

	// If weapon is broken don't do damage
	@EventHandler
	public void playerAttack(EntityDamageByEntityEvent event) {
		if (event.getDamage() == 0 || event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK || !(event.getEntity() instanceof LivingEntity)
						|| !(event.getDamager() instanceof Player) || event.getEntity().hasMetadata("NPC") || event.getDamager().hasMetadata("NPC"))
			return;
		Player player = (Player) event.getDamager();
		ItemStack item = player.getInventory().getItemInMainHand();

		DurabilityItem durItem = new DurabilityItem(player, item);

		if (durItem.isBroken())
			event.setCancelled(true);
	}
}
