package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.stat.data.StoredTagsData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import net.Indyuce.mmoitems.stat.type.InternalStat;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.version.VersionMaterial;

public class StoredTags extends InternalStat implements GemStoneStat {
	public StoredTags() {
		super("STORED_TAGS", VersionMaterial.OAK_SIGN.toItem(), "Stored Tags",
				new String[] { "You found a secret dev easter egg", "introduced during the 2020 epidemic!" }, new String[] { "all" });
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
		for (ItemTag tag : ((StoredTagsData) data).getTags())
			item.addItemTag(tag);
	}

	@Override
	public void whenLoaded(ReadMMOItem mmoitem) {
		mmoitem.setData(ItemStats.STORED_TAGS, new StoredTagsData(mmoitem.getNBT()));
	}
}
