package net.Indyuce.mmoitems.stat.type;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;

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
	public StatData whenInitialized(Object object) {
		// not supported
		return null;
	}

	@Override
	public RandomStatData whenInitializedGeneration(Object object) {
		// not supported
		return null;
	}

	@Override
	public void whenClicked(EditionInventory inv, InventoryClickEvent event) {
		// not supported
	}

	@Override
	public boolean whenInput(EditionInventory inv, ConfigFile config, String message, Object... info) {
		// not supported
		return true;
	}

	@Override
	public void whenDisplayed(List<String> lore, MMOItem mmoitem) {
		// not supported
	}
}
