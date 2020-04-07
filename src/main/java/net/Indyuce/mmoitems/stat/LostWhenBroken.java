package net.Indyuce.mmoitems.stat;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.BooleanStat;
import net.mmogroup.mmolib.api.item.ItemTag;

public class LostWhenBroken extends BooleanStat {
	public LostWhenBroken() {
		super("WILL_BREAK", new ItemStack(Material.SHEARS), "Lost when Broken?", new String[] { "If set to true, the item will be lost", "once it reaches 0 durability." }, new String[] { "all" });
	}

	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		if (((BooleanData) data).isEnabled())
			item.addItemTag(new ItemTag("MMOITEMS_WILL_BREAK", true));
		return true;
	}
}
