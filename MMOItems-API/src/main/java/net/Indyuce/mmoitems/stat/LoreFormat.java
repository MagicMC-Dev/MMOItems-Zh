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
		super("LORE_FORMAT", Material.MAP, "Lore 标注格式", new String[] { "Lore格式决定", "每个属性数据排版显示效果.", "&9在 language/lore-formats ", "&9中" },
				new String[] { "all" });
	}

	@Override
	public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StringData data) {
		String path = data.toString();
		Validate.isTrue(MMOItems.plugin.getLore().hasFormat(path), "找不到 ID 为 '" + path + "' 的 Lore 标注格式");

		item.addItemTag(new ItemTag(getNBTPath(), path));
	}

	@Override
	public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
		Validate.isTrue(MMOItems.plugin.getLore().hasFormat(message), "找不到 ID 为 '" + message + "' 的 Lore 标注格式");

		inv.getEditedSection().set(getPath(), message);
		inv.registerTemplateEdition();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Lore 标注格式成功更改为 " + message + "");
	}
}
