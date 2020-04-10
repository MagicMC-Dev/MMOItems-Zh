package net.Indyuce.mmoitems.stat;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.BooleanData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.BooleanStat;
import net.mmogroup.mmolib.api.item.ItemTag;

public class VanillaEatingAnimation extends BooleanStat {
	public VanillaEatingAnimation() {
		super("VANILLA_EATING", new ItemStack(Material.COOKED_BEEF), "Vanilla Eating Animation", new String[] { "When enabled, players have to wait", "for the vanilla eating animation", "in order to eat the consumable." }, new String[] { "consumable" });
	}

	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		if (((BooleanData) data).isEnabled())
			item.addItemTag(new ItemTag("MMOITEMS_VANILLA_EATING", true));
		return true;
	}
}
