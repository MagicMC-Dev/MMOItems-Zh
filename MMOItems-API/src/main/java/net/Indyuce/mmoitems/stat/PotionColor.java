package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.util.AltChar;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.ColorData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.meta.PotionMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PotionColor extends ItemStat<ColorData, ColorData> {
	public PotionColor() {
		super("POTION_COLOR", Material.POTION, "药水颜色",
				new String[] { "你的药水的颜色", " (不影响效果) " }, new String[] { "all" }, Material.POTION,
				Material.SPLASH_POTION, Material.LINGERING_POTION, Material.TIPPED_ARROW);
	}

	@Override
	public ColorData whenInitialized(Object object) {
		Validate.isTrue(object instanceof String, "必须指定一个字符串");
		return new ColorData((String) object);
	}

	@Override
	public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new StatEdition(inv, ItemStats.POTION_COLOR).enable("在聊天中输入您想要的 RGB 颜色",
					ChatColor.AQUA + "格式: {Red} {Green} {Blue}");

		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			inv.getEditedSection().set("potion-color", null);
			inv.registerTemplateEdition();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "成功移除药水颜色");
		}
	}

	@Override
	public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
		String[] split = message.split(" ");
		Validate.isTrue(split.length == 3, "使用这种格式: {Red} {Green} {Blue}. 例子: '75 0 130' 代表紫色.");

		for (String str : split) {
			int k = Integer.parseInt(str);
			Validate.isTrue(k >= 0 && k < 256, "颜色必须介于 0 到 255 之间");
		}

		inv.getEditedSection().set("potion-color", message);
		inv.registerTemplateEdition();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "药水颜色成功更改为 " + message + ".");
	}

	@Override
	public void whenLoaded(@NotNull ReadMMOItem mmoitem) {
		if (!(mmoitem.getNBT().getItem().getItemMeta() instanceof PotionMeta))
			return;

		final Color color = ((PotionMeta) mmoitem.getNBT().getItem().getItemMeta()).getColor();
		if (color != null)
			mmoitem.setData(this, new ColorData(color));
	}

	@Nullable
	@Override
	public ColorData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {
		throw new NotImplementedException();
	}

	@Override
	public void whenDisplayed(List<String> lore, Optional<ColorData> statData) {

		lore.add(statData.isPresent() ? ChatColor.GREEN + statData.get().toString() : ChatColor.RED + "Uncolored");

		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " 左键单击进行选择");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " 右键单击以删除药水颜色");
	}

	@NotNull
	@Override
	public ColorData getClearStatData() {
		return new ColorData(0,0,0);
	}

	@Override
	public void whenApplied(@NotNull ItemStackBuilder item, @NotNull ColorData data) {
		if (item.getItemStack().getType().name().contains("POTION") || item.getItemStack().getType() == Material.TIPPED_ARROW)
			((PotionMeta) item.getMeta()).setColor(data.getColor());
	}

	@NotNull
	@Override
	public ArrayList<ItemTag> getAppliedNBT(@NotNull ColorData data) {
		throw new NotImplementedException();
	}
}
