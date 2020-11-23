package net.Indyuce.mmoitems.stat.type;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;

public abstract class InternalStat extends ItemStat {

	/**
	 * Internal stats can be used to store specific item data and cannot be
	 * edited in the item edition GUI since they only exist once the item is
	 * physically generated.
	 */
	public InternalStat(String id, ItemStack item, String name, String[] lore, String[] types, Material... materials) {
		super(id, item, name, lore, types, materials);
	}

	@Override
	public RandomStatData whenInitialized(Object object) {
		// not supported
		return null;
	}

	@Override
	public void whenClicked(EditionInventory inv, InventoryClickEvent event) {
		// not supported
	}

	@Override
	public void whenInput(EditionInventory inv, String message, Object... info) {
		// not supported
	}

	@Override
	public void whenDisplayed(List<String> lore, RandomStatData statData) {
		// not supported
	}
}
