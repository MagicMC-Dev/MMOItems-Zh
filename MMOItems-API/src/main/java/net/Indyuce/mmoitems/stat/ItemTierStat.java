package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.StringStat;
import io.lumine.mythic.lib.api.item.ItemTag;
import org.jetbrains.annotations.NotNull;

public class ItemTierStat extends StringStat implements GemStoneStat {
	public ItemTierStat() {
		super("TIER", Material.DIAMOND, "稀有程度", new String[] { "该等级定义了您的物品的稀有程度", "以及物品被分解时会掉落的物品", "&9Tiers可以在tiers.yml文件中配置" }, new String[] { "all" });
	}

	@Override
	public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StringData data) {
		String path = data.toString().toUpperCase().replace("-", "_").replace(" ", "_");
		Validate.isTrue(MMOItems.plugin.getTiers().has(path), "找不到 ID 为 '" + path + "' 的稀有程度");

		ItemTier tier = MMOItems.plugin.getTiers().get(path);
		item.addItemTag(new ItemTag("MMOITEMS_TIER", path));
		item.getLore().insert("tier", MMOItems.plugin.getLanguage().getStatFormat(getPath()).replace("{value}", tier.getName()));
	}

	@Override
	public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
		String format = message.toUpperCase().replace(" ", "_").replace("-", "_");
		Validate.isTrue(MMOItems.plugin.getTiers().has(format), "找不到名为 '" + format + "' 的稀有程度");

		inv.getEditedSection().set("tier", format);
		inv.registerTemplateEdition();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "等级已成功更改为" + format + "");
	}
}
