package net.Indyuce.mmoitems.version.nms;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.item.NBTItem;

public interface NMSHandler {
	NBTItem getNBTItem(ItemStack item);

	void sendTitle(Player player, String title, String subtitle, int fadeIn, int ticks, int fadeOut);

	void sendActionBar(Player player, String message);

	void sendJson(Player player, String message);

	int getNextContainerId(Player player);

	void handleInventoryCloseEvent(Player player);

	void sendPacketOpenWindow(Player player, int containerId);

	void sendPacketCloseWindow(Player player, int containerId);

	void setActiveContainerDefault(Player player);

	void setActiveContainer(Player player, Object container);

	void setActiveContainerId(Object container, int containerId);

	void addActiveContainerSlotListener(Object container, Player player);

	void playArmAnimation(Player player);

	Inventory toBukkitInventory(Object container);

	Sound getBlockPlaceSound(Block block);

	Object newContainerAnvil(Player player);

	boolean isInBoundingBox(Entity entity, Location loc);

	double distanceSquaredFromBoundingBox(Entity entity, Location loc);

	default double distanceFromBoundingBox(Entity entity, Location loc) {
		return Math.sqrt(distanceFromBoundingBox(entity, loc));
	}
}
