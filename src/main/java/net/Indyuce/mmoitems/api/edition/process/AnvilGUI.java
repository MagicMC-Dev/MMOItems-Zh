package net.Indyuce.mmoitems.api.edition.process;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.edition.Edition;
import net.Indyuce.mmoitems.gui.PluginInventory;

public class AnvilGUI extends EditionProcess implements Listener {
	private final int containerId;
	private final Inventory inventory;
	private boolean open;

	public AnvilGUI(PluginInventory inv, Edition edition) {
		super(inv, edition);

		ItemStack paper = new ItemStack(Material.PAPER);
		ItemMeta paperMeta = paper.getItemMeta();
		paperMeta.setDisplayName("Input text..");
		paper.setItemMeta(paperMeta);

		MMOItems.plugin.getNMS().handleInventoryCloseEvent(getPlayer());
		MMOItems.plugin.getNMS().setActiveContainerDefault(getPlayer());

		Bukkit.getPluginManager().registerEvents(this, MMOItems.plugin);

		final Object container = MMOItems.plugin.getNMS().newContainerAnvil(getPlayer());

		inventory = MMOItems.plugin.getNMS().toBukkitInventory(container);
		inventory.setItem(0, paper);

		containerId = MMOItems.plugin.getNMS().getNextContainerId(getPlayer());
		MMOItems.plugin.getNMS().sendPacketOpenWindow(getPlayer(), containerId);
		MMOItems.plugin.getNMS().setActiveContainer(getPlayer(), container);
		MMOItems.plugin.getNMS().setActiveContainerId(container, containerId);
		MMOItems.plugin.getNMS().addActiveContainerSlotListener(container, getPlayer());

		open = true;
	}

	public Inventory getInventory() {
		return inventory;
	}

	@Override
	public void close() {
		if (!open)
			return;
		open = false;

		MMOItems.plugin.getNMS().handleInventoryCloseEvent(getPlayer());
		MMOItems.plugin.getNMS().setActiveContainerDefault(getPlayer());
		MMOItems.plugin.getNMS().sendPacketCloseWindow(getPlayer(), containerId);

		HandlerList.unregisterAll(this);
	}

	@EventHandler
	public void a(InventoryClickEvent event) {
		if (event.getInventory().equals(inventory)) {
			event.setCancelled(true);
			if (event.getRawSlot() == 2) {
				ItemStack clicked = inventory.getItem(event.getRawSlot());
				if (clicked != null && clicked.getType() != Material.AIR)
					input(clicked.hasItemMeta() ? clicked.getItemMeta().getDisplayName() : clicked.getType().toString());
			}
		}
	}

	@EventHandler
	public void b(InventoryCloseEvent event) {
		if (open && event.getInventory().equals(inventory))
			close();
	}
}