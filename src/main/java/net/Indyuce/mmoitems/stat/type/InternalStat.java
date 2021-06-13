package net.Indyuce.mmoitems.stat.type;

import java.util.List;
import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;

import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import org.jetbrains.annotations.NotNull;

public abstract class InternalStat extends ItemStat {

	/**
	 * Internal stats can be used to store specific item data and cannot be
	 * edited in the item edition GUI since they only exist once the item is
	 * physically generated.
	 */
	public InternalStat(String id, Material mat, String name, String[] lore, String[] types, Material... materials) {
		super(id, mat, name, lore, types, materials);
	}

	@Override
	public RandomStatData whenInitialized(Object object) {
		// not supported
		return null;
	}

	@Override
	public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
		// not supported
	}

	@Override
	public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
		// not supported
	}

	@Override
	public void whenDisplayed(List<String> lore, Optional<RandomStatData> statData) {
		// not supported
	}
}
