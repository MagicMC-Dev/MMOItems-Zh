package net.Indyuce.mmoitems.stat;

import org.bukkit.Material;

import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.stat.data.BooleanData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.BooleanStat;
import io.lumine.mythic.lib.api.item.ItemTag;
import org.jetbrains.annotations.NotNull;

public class VanillaEatingAnimation extends BooleanStat {
	public VanillaEatingAnimation() {
		super("VANILLA_EATING", Material.COOKED_BEEF, "原版食用动画", new String[] { "启用后，玩家必须等待原版", "食用动画结束才能吃消耗品,", "仅适用于通常可以食用的物品"}, new String[] { "consumable" });
	}

	@Override
	public void whenApplied(@NotNull ItemStackBuilder item, @NotNull BooleanData data) {
		if (data.isEnabled())
			item.addItemTag(new ItemTag("MMOITEMS_VANILLA_EATING", true));
	}
}
