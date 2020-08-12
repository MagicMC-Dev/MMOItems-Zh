package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.InternalStat;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.version.VersionMaterial;

public class ItemLevel extends InternalStat {
	public ItemLevel() {
		super("ITEM_LEVEL", VersionMaterial.EXPERIENCE_BOTTLE.toItem(), "Item Level", new String[] { "The item level" }, new String[] { "all" });
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
		item.addItemTag(new ItemTag("MMOITEMS_ITEM_LEVEL", (int) ((DoubleData) data).getValue()));
	}

	@Override
	public void whenLoaded(ReadMMOItem mmoitem) {
		if (mmoitem.getNBT().hasTag("MMOITEMS_ITEM_LEVEL"))
			mmoitem.setData(this, new DoubleData(mmoitem.getNBT().getDouble("MMOITEMS_ITEM_LEVEL")));
	}
}
