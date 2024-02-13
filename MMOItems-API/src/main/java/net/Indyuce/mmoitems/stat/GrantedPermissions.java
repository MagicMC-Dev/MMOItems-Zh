package net.Indyuce.mmoitems.stat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StringListData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import net.Indyuce.mmoitems.stat.type.StringListStat;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.util.AltChar;
import org.jetbrains.annotations.NotNull;

public class GrantedPermissions extends StringListStat implements GemStoneStat {
	public GrantedPermissions() {
		super("GRANTED_PERMISSIONS", Material.NAME_TAG, "授予的权限",
				new String[] { "持有物品将授予的权限列表" }, new String[] { "all" });
	}

	@Override
	@SuppressWarnings("unchecked")
	public StringListData whenInitialized(Object object) {
		Validate.isTrue(object instanceof List<?>, "必须指定一个字符串列表");
		return new StringListData((List<String>) object);
	}

	@Override
	public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new StatEdition(inv, ItemStats.GRANTED_PERMISSIONS).enable("在聊天中输入您要添加的权限");

		if (event.getAction() == InventoryAction.PICKUP_HALF && inv.getEditedSection().contains(getPath())) {
			List<String> permissions = inv.getEditedSection().getStringList(getPath());
			if (permissions.isEmpty())
				return;

			String last = permissions.get(permissions.size() - 1);
			permissions.remove(last);
			inv.getEditedSection().set(getPath(), permissions.isEmpty() ? null : permissions);
			inv.registerTemplateEdition();
			inv.getPlayer()
					.sendMessage(MMOItems.plugin.getPrefix() + "成功删除'" + MythicLib.plugin.parseColors(last) + ChatColor.GRAY + "'.");
		}
	}

	@Override
	public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
		List<String> permissions = inv.getEditedSection().contains(getPath()) ? inv.getEditedSection().getStringList(getPath()) : new ArrayList<>();
		permissions.add(message);
		inv.getEditedSection().set(getPath(), permissions);
		inv.registerTemplateEdition();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "权限添加成功");
	}

	@Override
	public void whenDisplayed(List<String> lore, Optional<StringListData> statData) {

		if (statData.isPresent()) {
			lore.add(ChatColor.GRAY + "Current Value:");
			StringListData data = statData.get();
			data.getList().forEach(element -> lore.add(ChatColor.GRAY + element));

		} else
			lore.add(ChatColor.GRAY + "当前值: " + ChatColor.RED + "None");

		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " 单击以添加权限");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " 右键单击删除最后一个权限");
	}
}
