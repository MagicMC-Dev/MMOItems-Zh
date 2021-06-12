package net.Indyuce.mmoitems.stat;

import java.util.ArrayList;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.InternalStat;

public class ItemLevel extends InternalStat {
	public ItemLevel() {
		super("ITEM_LEVEL", VersionMaterial.EXPERIENCE_BOTTLE.toMaterial(), "Item Level", new String[] { "The item level" }, new String[] { "all" });
	}

	@Override
	public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StatData data) { item.addItemTag(getAppliedNBT(data)); }

	@NotNull
	@Override
	public ArrayList<ItemTag> getAppliedNBT(@NotNull StatData data) {

		// Array
		ArrayList<ItemTag> ret = new ArrayList<>();

		// Add
		ret.add(new ItemTag(getNBTPath(), ((DoubleData) data).getValue()));

		return ret;
	}

	@Override
	public void whenLoaded(@NotNull ReadMMOItem mmoitem) {

		// Find relevant tags
		ArrayList<ItemTag> relevantTags = new ArrayList<>();
		if (mmoitem.getNBT().hasTag(getNBTPath()))
			relevantTags.add(ItemTag.getTagAtPath(getNBTPath(), mmoitem.getNBT(), SupportedNBTTagValues.DOUBLE));

		// Build StatData
		StatData data = getLoadedNBT(relevantTags);

		if (data != null) { mmoitem.setData(this, data); }
	}

	@Nullable
	@Override
	public StatData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {

		// Get tag
		ItemTag lTag = ItemTag.getTagAtPath(getNBTPath(), storedTags);

		// Found?
		if (lTag != null) {

			// Thats it
			return new DoubleData((double) lTag.getValue());
		}

		// Nope
		return null;
	}

	@NotNull
	@Override
	public StatData getClearStatData() {
		return new DoubleData(0D);
	}
}
