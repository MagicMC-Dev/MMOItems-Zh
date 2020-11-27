package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.util.AltChar;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public class RevisionID extends ItemStat {
	public RevisionID() {
		super("REVISION_ID", Material.ITEM_FRAME, "Revision ID", new String[] { "" }, new String[] { "all" });
	}

	@Override
	public RandomStatData whenInitialized(Object object) {
		if (object instanceof Integer)
			return new NumericStatFormula((Integer) object, 0, 0, 0);

		throw new IllegalArgumentException("Must specify a whole number");
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
		item.addItemTag(new ItemTag("MMOITEMS_REVISION_ID", (int) ((DoubleData) data).getValue()));
	}

	@Override
	public void whenClicked(EditionInventory inv, InventoryClickEvent event) {
		int id = inv.getEditedSection().getInt(getPath(), 1);
		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			inv.getEditedSection().set(getPath(), Math.min(id + 1, Integer.MAX_VALUE));
			inv.registerTemplateEdition();
			return;
		}

		inv.getEditedSection().set(getPath(), Math.max(id - 1, 1));
		inv.registerTemplateEdition();
	}

	@Override
	public void whenInput(EditionInventory inv, String message, Object... info) {}

	@Override
	public void whenLoaded(ReadMMOItem mmoitem) {
		if (mmoitem.getNBT().hasTag("MMOITEMS_REVISION_ID"))
			mmoitem.setData(this, new DoubleData(mmoitem.getNBT().getInteger("MMOITEMS_REVISION_ID")));
	}

	@Override
	public void whenDisplayed(List<String> lore, RandomStatData statData) {
		if (statData.isPresent()) {
			NumericStatFormula data = (NumericStatFormula) statData;
			lore.add(ChatColor.GRAY + "Current Revision ID: " + ChatColor.GREEN + ((int) data.getBase()));
		} else
			lore.add(ChatColor.GRAY + "Current Revision ID: " + ChatColor.GREEN + "1");

		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Left click to decrease this value.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to increase this value.");
	}
}
