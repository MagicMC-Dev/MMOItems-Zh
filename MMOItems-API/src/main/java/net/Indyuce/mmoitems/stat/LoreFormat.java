package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.data.StringListData;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.StringStat;
import io.lumine.mythic.lib.api.item.ItemTag;
import org.jetbrains.annotations.NotNull;

public class LoreFormat extends StringStat implements GemStoneStat {
	public LoreFormat() {
		super("LORE_FORMAT", Material.MAP, "标注格式", new String[] { "标注格式决定了每个统计数据的去向", "&9格式可以在", "&9lore-formats文件夹中配置" },
				new String[] { "all" });
	}

	@Override
	public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StringData data) {
		String path = data.toString();
		Validate.isTrue(MMOItems.plugin.getFormats().hasFormat(path), "找不到 ID 为 '" + path + "' 的标注格式");

		item.addItemTag(new ItemTag(getNBTPath(), path));
	}

	@Override
	public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
		Validate.isTrue(MMOItems.plugin.getFormats().hasFormat(message), "找不到 ID 为 '" + message + "'的标注格式");

		inv.getEditedSection().set(getPath(), message);
		inv.registerTemplateEdition();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "标注格式成功更改为" + message + "");
	}
}
