package net.Indyuce.mmoitems.comp.inventory;

import java.util.List;

import org.bukkit.entity.Player;

import net.Indyuce.mmoitems.api.player.inventory.EquippedItem;

public interface PlayerInventory {
	List<EquippedItem> getInventory(Player player);
}
