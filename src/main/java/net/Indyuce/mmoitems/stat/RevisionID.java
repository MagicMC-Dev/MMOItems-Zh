package net.Indyuce.mmoitems.stat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import net.Indyuce.mmoitems.api.util.MMOItemReforger;
import net.Indyuce.mmoitems.gui.edition.AbilityListEdition;
import net.Indyuce.mmoitems.gui.edition.RevisionInventory;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;

import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.util.AltChar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import scala.math.Numeric;

/**
 * Regarding the auto updating of items
 * @see RevisionInventory
 * @see MMOItemReforger
 */
public class RevisionID extends ItemStat<NumericStatFormula, DoubleData> implements GemStoneStat {
	public RevisionID() {
		super("REVISION_ID", Material.ITEM_FRAME, "Revision ID", new String[] { "The Revision ID is used to determine",
		"if an item is outdated or not. You", "should increase this whenever", "you make changes to your item!", "", "\u00a76The updater is smart and will apply", "\u00a76changes to the base stats of the item,", "\u00a76keeping gemstones intact (for example)."},
				new String[] { "all" });
	}

	@Override
	public NumericStatFormula whenInitialized(Object object) {
		if (object instanceof Integer)
			return new NumericStatFormula((Integer) object, 0, 0, 0);

		throw new IllegalArgumentException("Must specify a whole number");
	}

	@Override
	public void whenApplied(@NotNull ItemStackBuilder item, @NotNull DoubleData data) {
		item.addItemTag(getAppliedNBT(data));
	}

	@NotNull
	@Override
	public ArrayList<ItemTag> getAppliedNBT(@NotNull DoubleData data) {
		ArrayList<ItemTag> ret = new ArrayList<>();
		ret.add(new ItemTag(getNBTPath(), (int) data.getValue()));
		return ret;
	}

	@Override
	public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
		new RevisionInventory(inv.getPlayer(), inv.getEdited()).open(inv.getPage());
	}

	@Override
	public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) { }

	@Override
	public void whenLoaded(@NotNull ReadMMOItem mmoitem) {
		ArrayList<ItemTag> tags = new ArrayList<>();
		if (mmoitem.getNBT().hasTag(getNBTPath()))
			tags.add(ItemTag.getTagAtPath(getNBTPath(), mmoitem.getNBT(), SupportedNBTTagValues.INTEGER));
		DoubleData data = getLoadedNBT(tags);
		if (data != null) { mmoitem.setData(this, data); }
	}

	@Nullable
	@Override
	public DoubleData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {
		ItemTag tag = ItemTag.getTagAtPath(getNBTPath(), storedTags);
		if (tag != null) { return new DoubleData((int) tag.getValue()); }
		return null;
	}

	@Override
	public void whenDisplayed(List<String> lore, Optional<NumericStatFormula> statData) {
		if (statData.isPresent()) {
			NumericStatFormula data = (NumericStatFormula) statData.get();
			lore.add(ChatColor.GRAY + "Current Revision ID: " + ChatColor.GREEN + ((int) data.getBase()));
		} else
			lore.add(ChatColor.GRAY + "Current Revision ID: " + ChatColor.GREEN + "1");

		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Left click to increase this value.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to decrease this value.");
	}

	@NotNull
	@Override
	public DoubleData getClearStatData() {
		return new DoubleData(0D);
	}
}
