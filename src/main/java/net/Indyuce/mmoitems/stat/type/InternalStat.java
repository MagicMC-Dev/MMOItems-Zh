package net.Indyuce.mmoitems.stat.type;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;

public abstract class InternalStat extends ItemStat {

	/*
	 * internal stats can be used to store specific item data and cannot be
	 * edited in the item edition GUI since they only exist once the item is
	 * physically generated.
	 */
	public InternalStat(String id, ItemStack item, String name, String[] lore, String[] types, Material... materials) {
		super(id, item, name, lore, types, materials);
	}

	@Override
	public void whenLoaded(MMOItem item, ConfigurationSection config) {
		// not supported
	}

	@Override
	public boolean whenClicked(EditionInventory inv, InventoryClickEvent event) {
		// not supported
		return true;
	}

	@Override
	public boolean whenInput(EditionInventory inv, ConfigFile config, String message, Object... info) {
		// not supported
		return true;
	}

	@Override
	public void whenDisplayed(List<String> lore, FileConfiguration config, String id) {
		// not supported
	}
}
