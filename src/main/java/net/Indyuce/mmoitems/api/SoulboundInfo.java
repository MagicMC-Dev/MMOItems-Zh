package net.Indyuce.mmoitems.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SoulboundInfo {
	
	/*
	 * items that need to be given to the player whenever he respawns.
	 */
	private final List<ItemStack> items = new ArrayList<>();

	/*
	 * if the player leaves the server before removing, the cached items will be
	 * lost. the plugin saves the last location of the player to drop the items
	 * when the server shuts down this way they are 'saved'
	 */
	private final Location loc;
	private final Player player;

	private static Map<UUID, SoulboundInfo> info = new HashMap<>();

	public SoulboundInfo(Player player) {
		this.player = player;
		loc = player.getLocation().clone();
	}

	public void add(ItemStack item) {
		items.add(item);
	}

	public void setup() {
		if (hasItems())
			info.put(player.getUniqueId(), this);
	}
	
	public static Collection<SoulboundInfo> getAbandonnedInfo() {
		return info.values();
	}

	public void giveItems() {
		for (ItemStack item : items)
			for (ItemStack drop : player.getInventory().addItem(item).values())
				player.getWorld().dropItem(player.getLocation(), drop);
	}

	public boolean hasItems() {
		return !items.isEmpty();
	}

	public void dropItems() {
		items.forEach(item -> loc.getWorld().dropItem(loc, item));
	}

	public static void read(Player player) {
		if (info.containsKey(player.getUniqueId())) {
			info.get(player.getUniqueId()).giveItems();
			info.remove(player.getUniqueId());
		}
	}
}
