package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.stat.type.BooleanStat;
import org.bukkit.Material;

public class LostWhenBroken extends BooleanStat {
	public LostWhenBroken() {
		super("WILL_BREAK", Material.SHEARS, "耐久用尽后消失?", new String[] {"带有自定义耐久度的物品在耐久度降至0时默认不会破损。", "请开启此选项以使您的物品破损。默认情况下，", "普通物品在耐久度降至0时会破损。请开启此选项以使其保持完好。" }, new String[] { "!block", "all" });
	}
}
