package net.Indyuce.mmoitems.api.edition;

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
import net.Indyuce.mmoitems.api.edition.StatEdition.StatEditionProcess;

public class AnvilGUI implements StatEditionProcess, Listener {
	private StatEdition edition;

	private int containerId;
	private Inventory inventory;
	private boolean open;

	public Inventory getInventory() {
		return inventory;
	}

	@Override
	public void open(StatEdition edition) {
		this.edition = edition;

		ItemStack paper = new ItemStack(Material.PAPER);
		ItemMeta paperMeta = paper.getItemMeta();
		paperMeta.setDisplayName("Input text..");
		paper.setItemMeta(paperMeta);

		MMOItems.plugin.getNMS().handleInventoryCloseEvent(edition.getPlayer());
		MMOItems.plugin.getNMS().setActiveContainerDefault(edition.getPlayer());

		Bukkit.getPluginManager().registerEvents(this, MMOItems.plugin);

		final Object container = MMOItems.plugin.getNMS().newContainerAnvil(edition.getPlayer());

		inventory = MMOItems.plugin.getNMS().toBukkitInventory(container);
		inventory.setItem(0, paper);

		containerId = MMOItems.plugin.getNMS().getNextContainerId(edition.getPlayer());
		MMOItems.plugin.getNMS().sendPacketOpenWindow(edition.getPlayer(), containerId);
		MMOItems.plugin.getNMS().setActiveContainer(edition.getPlayer(), container);
		MMOItems.plugin.getNMS().setActiveContainerId(container, containerId);
		MMOItems.plugin.getNMS().addActiveContainerSlotListener(container, edition.getPlayer());

		open = true;
	}

	@Override
	public void close() {
		if (!open)
			return;
		open = false;

		MMOItems.plugin.getNMS().handleInventoryCloseEvent(edition.getPlayer());
		MMOItems.plugin.getNMS().setActiveContainerDefault(edition.getPlayer());
		MMOItems.plugin.getNMS().sendPacketCloseWindow(edition.getPlayer(), containerId);

		HandlerList.unregisterAll(this);
	}

	@EventHandler
	public void a(InventoryClickEvent event) {
		if (event.getInventory().equals(inventory)) {
			event.setCancelled(true);
			if (event.getRawSlot() == 2) {
				ItemStack clicked = inventory.getItem(event.getRawSlot());
				if (clicked != null && clicked.getType() != Material.AIR)
					edition.output(clicked.hasItemMeta() ? clicked.getItemMeta().getDisplayName() : clicked.getType().toString());
			}
		}
	}

	@EventHandler
	public void b(InventoryCloseEvent event) {
		if (open && event.getInventory().equals(inventory))
			close();
	}
}