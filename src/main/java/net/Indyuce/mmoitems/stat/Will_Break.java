package net.Indyuce.mmoitems.stat;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.Indyuce.mmoitems.stat.type.BooleanStat;
import net.mmogroup.mmolib.api.item.ItemTag;

public class Will_Break extends BooleanStat {
	public Will_Break() {
		super(new ItemStack(Material.SHEARS), "Will Break?", new String[] { "If set to true, the item will break", "once it reaches 0 durability.", "&c&oOnly works with custom durability." }, "will-break", new String[] { "all" });
	}

	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		if (((BooleanData) data).isEnabled())
			item.addItemTag(new ItemTag("MMOITEMS_WILL_BREAK", true));
		return true;
	}
}
