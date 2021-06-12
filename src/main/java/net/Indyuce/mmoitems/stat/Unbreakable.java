package net.Indyuce.mmoitems.stat;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.stat.data.BooleanData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.BooleanStat;

public class Unbreakable extends BooleanStat {
	public Unbreakable() {
		super("UNBREAKABLE", Material.ANVIL, "Unbreakable", new String[] { "Infinite durability if set to true." }, new String[] { "all" });
	}

	@Override
	public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StatData data) {
		if (((BooleanData) data).isEnabled()) {

			// Hide unbreakable if
			item.addItemTag(getAppliedNBT(data));
			item.getMeta().addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		}
	}

	@NotNull
	@Override
	public ArrayList<ItemTag> getAppliedNBT(@NotNull StatData data) {
		ArrayList<ItemTag> a = new ArrayList<>();
		if (((BooleanData) data).isEnabled()) { a.add(new ItemTag(getNBTPath(), true)); }
		return a;
	}

	@Override
	public void whenLoaded(@NotNull ReadMMOItem mmoitem) {
		ArrayList<ItemTag> rTags = new ArrayList<>();
		if (mmoitem.getNBT().hasTag(getNBTPath()))
			rTags.add(ItemTag.getTagAtPath(getNBTPath(), mmoitem.getNBT(), SupportedNBTTagValues.BOOLEAN));
		StatData data = getLoadedNBT(rTags);
		if (data != null) { mmoitem.setData(this, data);}
	}

	@Nullable
	@Override
	public StatData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {

		ItemTag uTag = ItemTag.getTagAtPath(getNBTPath(), storedTags);

		if (uTag != null) {

			// As Boolean
			return new BooleanData((boolean) uTag.getValue());
		}

		return null;
	}

	@Override
	@NotNull public String getNBTPath() {
		return "Unbreakable";
	}
}
