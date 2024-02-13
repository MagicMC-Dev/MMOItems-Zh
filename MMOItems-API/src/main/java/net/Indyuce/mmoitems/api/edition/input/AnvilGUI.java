package net.Indyuce.mmoitems.api.edition.input;

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
import io.lumine.mythic.lib.MythicLib;

@Deprecated
public class AnvilGUI extends PlayerInputHandler implements Listener {
	private final int containerId;
	private final Inventory inventory;

	private boolean open;

	/**
	 * Allows to retrieve player input using an anvil GUI
	 * 
	 * @param edition Data being edited
	 */
	@Deprecated
	public AnvilGUI(Edition edition) {
		super(edition);

		ItemStack paper = new ItemStack(Material.PAPER);
		ItemMeta paperMeta = paper.getItemMeta();
		paperMeta.setDisplayName("输入文字..");
		paper.setItemMeta(paperMeta);

		MythicLib.plugin.getVersion().getWrapper().handleInventoryCloseEvent(getPlayer());
		MythicLib.plugin.getVersion().getWrapper().setActiveContainerDefault(getPlayer());

		Bukkit.getPluginManager().registerEvents(this, MMOItems.plugin);

		final Object container = MythicLib.plugin.getVersion().getWrapper().newContainerAnvil(getPlayer());

		inventory = MythicLib.plugin.getVersion().getWrapper().toBukkitInventory(container);
		inventory.setItem(0, paper);

		containerId = MythicLib.plugin.getVersion().getWrapper().getNextContainerId(getPlayer());
		MythicLib.plugin.getVersion().getWrapper().sendPacketOpenWindow(getPlayer(), containerId);
		MythicLib.plugin.getVersion().getWrapper().setActiveContainer(getPlayer(), container);
		MythicLib.plugin.getVersion().getWrapper().setActiveContainerId(container, containerId);
		MythicLib.plugin.getVersion().getWrapper().addActiveContainerSlotListener(container, getPlayer());

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

		MythicLib.plugin.getVersion().getWrapper().handleInventoryCloseEvent(getPlayer());
		MythicLib.plugin.getVersion().getWrapper().setActiveContainerDefault(getPlayer());
		MythicLib.plugin.getVersion().getWrapper().sendPacketCloseWindow(getPlayer(), containerId);

		HandlerList.unregisterAll(this);
	}

	@EventHandler
	public void a(InventoryClickEvent event) {
		if (event.getInventory().equals(inventory)) {
			event.setCancelled(true);
			if (event.getRawSlot() == 2) {
				ItemStack clicked = inventory.getItem(event.getRawSlot());
				if (clicked != null && clicked.getType() != Material.AIR)
					registerInput(clicked.hasItemMeta() ? clicked.getItemMeta().getDisplayName() : clicked.getType().toString());
			}
		}
	}

	@EventHandler
	public void b(InventoryCloseEvent event) {
		if (open && event.getInventory().equals(inventory))
			close();
	}
}