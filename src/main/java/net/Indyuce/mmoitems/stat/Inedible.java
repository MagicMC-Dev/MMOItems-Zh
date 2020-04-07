package net.Indyuce.mmoitems.stat;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.BooleanStat;
import net.mmogroup.mmolib.api.item.ItemTag;

public class Inedible extends BooleanStat {
	public Inedible() {
		super("INEDIBLE", new ItemStack(Material.POISONOUS_POTATO), "Inedible", new String[] { "Players won't be able to", "right-click this consumable." }, new String[] { "consumable" });
	}

	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		if (((BooleanData) data).isEnabled())
			item.addItemTag(new ItemTag("MMOITEMS_INEDIBLE", true));
		return true;
	}
}
