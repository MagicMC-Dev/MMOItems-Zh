package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import io.lumine.mythic.lib.api.item.ItemTag;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class CustomModelData extends DoubleStat implements GemStoneStat {
	public CustomModelData() {
		super("CUSTOM_MODEL_DATA", Material.PAINTING, "自定义模型", new String[] { "您的 1.14+ 模型数据" }, new String[] { "!block", "all" });
	}

	@Override
	public void whenApplied(@NotNull ItemStackBuilder item, @NotNull DoubleData data) {

		// Edit meta
		item.getMeta().setCustomModelData((int) data.getValue());

		// Apply Custom Model Data
		item.addItemTag(getAppliedNBT(data));
	}

	@Override
	public void whenPreviewed(@NotNull ItemStackBuilder item, @NotNull DoubleData currentData, @NotNull NumericStatFormula templateData) throws IllegalArgumentException { whenApplied(item, currentData); }

	@NotNull
	@Override public ArrayList<ItemTag> getAppliedNBT(@NotNull DoubleData data) {

		// Make new ArrayList
		ArrayList<ItemTag> ret = new ArrayList<>();

		// Add Integer
		ret.add(new ItemTag(getNBTPath(), (int) data.getValue()));

		// Return thay
		return ret;
	}

	@Override
	public void whenLoaded(@NotNull ReadMMOItem mmoitem) {

		// Get Relevant tags
		ArrayList<ItemTag> relevantTags = new ArrayList<>();
		if (mmoitem.getNBT().hasTag(getNBTPath()))
			relevantTags.add(ItemTag.getTagAtPath(getNBTPath(), mmoitem.getNBT(), SupportedNBTTagValues.INTEGER));

		// Attempt to build data
		StatData data = getLoadedNBT(relevantTags);

		// Success?
		if (data != null) { mmoitem.setData(this, data);}
	}

	@Nullable
	@Override
	public DoubleData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {

		// Find Tag
		ItemTag cmd = ItemTag.getTagAtPath(getNBTPath(), storedTags);

		// Found?
		if (cmd != null) {

			// Well thats it
			return new DoubleData((Integer) cmd.getValue());
		}

		return null;

	}
}
