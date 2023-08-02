package net.Indyuce.mmoitems.gui.edition;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.util.MMOUtils;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.item.util.NamedItemStack;
import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.version.VersionMaterial;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class UpgradingEdition extends EditionInventory {
	private static final ItemStack notAvailable = new NamedItemStack(VersionMaterial.RED_STAINED_GLASS_PANE.toMaterial(), "&cNot Available");

	public UpgradingEdition(Player player, MMOItemTemplate template) {
		super(player, template);
	}

	@Override
	public Inventory getInventory() {
		Inventory inv = Bukkit.createInventory(this, 54, "升级设置: " + template.getId());

		boolean workbench = getEditedSection().getBoolean("upgrade.workbench");
		if (!template.getType().corresponds(Type.CONSUMABLE)) {

			ItemStack workbenchItem = new ItemStack(VersionMaterial.CRAFTING_TABLE.toMaterial());
			ItemMeta workbenchItemMeta = workbenchItem.getItemMeta();
			workbenchItemMeta.setDisplayName(ChatColor.GREEN + "仅工作台升级?");
			List<String> workbenchItemLore = new ArrayList<>();
			workbenchItemLore.add(ChatColor.GRAY + "开启后, 玩家必须使用制作");
			workbenchItemLore.add(ChatColor.GRAY + "站配方才能升级他们的武器");
			workbenchItemLore.add("");
			workbenchItemLore.add(ChatColor.GRAY + "当前值: " + ChatColor.GOLD + workbench);
			workbenchItemLore.add("");
			workbenchItemLore.add(ChatColor.YELLOW + AltChar.listDash + "左键单击进行选择");
			workbenchItemMeta.setLore(workbenchItemLore);
			workbenchItem.setItemMeta(workbenchItemMeta);
			inv.setItem(20, workbenchItem);

			String upgradeTemplate = getEditedSection().getString("upgrade.template");
			ItemStack templateItem = new ItemStack(VersionMaterial.OAK_SIGN.toMaterial());
			ItemMeta templateItemMeta = templateItem.getItemMeta();
			templateItemMeta.setDisplayName(ChatColor.GREEN + "升级模板");
			List<String> templateItemLore = new ArrayList<>();
			templateItemLore.add(ChatColor.GRAY + "此选项规定了哪些统计数据需要改进");
			templateItemLore.add(ChatColor.GRAY + "当您的物品升级时,更多信息请参见维基百科");
			templateItemLore.add("");
			templateItemLore.add(ChatColor.GRAY + "当前值: "
					+ (upgradeTemplate == null ? ChatColor.RED + "无模板" : ChatColor.GOLD + upgradeTemplate));
			templateItemLore.add("");
			templateItemLore.add(ChatColor.YELLOW + AltChar.listDash + "点击输入模板");
			templateItemLore.add(ChatColor.YELLOW + AltChar.listDash + "右键单击重置");
			templateItemMeta.setLore(templateItemLore);
			templateItem.setItemMeta(templateItemMeta);
			inv.setItem(22, templateItem);

			int max = getEditedSection().getInt("upgrade.max");
			ItemStack maxItem = new ItemStack(Material.BARRIER);
			ItemMeta maxItemMeta = maxItem.getItemMeta();
			maxItemMeta.setDisplayName(ChatColor.GREEN + "最大升级数");
			List<String> maxItemLore = new ArrayList<>();
			maxItemLore.add(ChatColor.GRAY + "您的物品可能获得的最大");
			maxItemLore.add(ChatColor.GRAY + "升级量 (配方或消耗品 ) ");
			maxItemLore.add("");
			maxItemLore.add(ChatColor.GRAY + "当前值: " + (max == 0 ? ChatColor.RED + "无限制" : ChatColor.GOLD + "" + max));
			maxItemLore.add("");
			maxItemLore.add(ChatColor.YELLOW + AltChar.listDash + "左键单击以尝试该值");
			maxItemLore.add(ChatColor.YELLOW + AltChar.listDash + "右键单击重置");
			maxItemMeta.setLore(maxItemLore);
			maxItem.setItemMeta(maxItemMeta);
			inv.setItem(40, maxItem);

			int min = getEditedSection().getInt("upgrade.min", 0);
			ItemStack minItem = new ItemStack(Material.BARRIER);
			ItemMeta minItemMeta = minItem.getItemMeta();
			minItemMeta.setDisplayName(ChatColor.GREEN + "最小升级次数");
			List<String> minItemLore = new ArrayList<>();
			minItemLore.add(ChatColor.GRAY + "您的物品可以降级到的最低");
			minItemLore.add(ChatColor.GRAY + "级别 (通过死亡或损坏 ) ");
			minItemLore.add("");
			minItemLore.add(ChatColor.GRAY + "当前值: " + (min == 0 ? ChatColor.RED + "0" : ChatColor.GOLD + String.valueOf(min)));
			minItemLore.add("");
			minItemLore.add(ChatColor.YELLOW + AltChar.listDash + "左键单击以尝试该值");
			minItemLore.add(ChatColor.YELLOW + AltChar.listDash + "右键单击重置");
			minItemMeta.setLore(minItemLore);
			minItem.setItemMeta(minItemMeta);
			inv.setItem(41, minItem);
		} else {
			inv.setItem(20, notAvailable);
			inv.setItem(22, notAvailable);
		}

		if (!workbench || template.getType().corresponds(Type.CONSUMABLE)) {

			String reference = getEditedSection().getString("upgrade.reference");
			ItemStack referenceItem = new ItemStack(Material.PAPER);
			ItemMeta referenceItemMeta = referenceItem.getItemMeta();
			referenceItemMeta.setDisplayName(ChatColor.GREEN + "升级参考");
			List<String> referenceItemLore = new ArrayList<>();
			referenceItemLore.add(ChatColor.GRAY + "该选项规定了消耗品可以");
			referenceItemLore.add(ChatColor.GRAY + "升级你的物品" + ChatColor.AQUA + "消耗品升级参考");
			referenceItemLore.add(ChatColor.AQUA + "必须与您的商品参考相匹配" + ChatColor.GRAY + ",");
			referenceItemLore.add(ChatColor.GRAY + "否则无法升级,将此留空");
			referenceItemLore.add(ChatColor.GRAY + "任何消耗品都可以升级这个物品");
			referenceItemLore.add("");
			referenceItemLore
					.add(ChatColor.GRAY + "当前值: " + (reference == null ? ChatColor.RED + "无参考" : ChatColor.GOLD + reference));
			referenceItemLore.add("");
			referenceItemLore.add(ChatColor.YELLOW + AltChar.listDash + "左键点击输入参考");
			referenceItemLore.add(ChatColor.YELLOW + AltChar.listDash + "右键单击重置");
			referenceItemMeta.setLore(referenceItemLore);
			referenceItem.setItemMeta(referenceItemMeta);
			inv.setItem(38, referenceItem);
		} else
			inv.setItem(38, notAvailable);

		double success = getEditedSection().getDouble("upgrade.success");
		ItemStack successItem = new ItemStack(VersionMaterial.EXPERIENCE_BOTTLE.toMaterial());
		ItemMeta successItemMeta = successItem.getItemMeta();
		successItemMeta.setDisplayName(ChatColor.GREEN + "成功机率");
		List<String> successItemLore = new ArrayList<>();
		successItemLore.add(ChatColor.GRAY + "使用消耗品或使用站升级");
		successItemLore.add(ChatColor.GRAY + "配方时成功升级的几率");
		successItemLore.add("");
		successItemLore.add(ChatColor.GRAY + "当前值: " + ChatColor.GOLD + (success == 0 ? "100" : "" + success) + "%");
		successItemLore.add("");
		successItemLore.add(ChatColor.YELLOW + AltChar.listDash + "左键单击可更改此值");
		successItemLore.add(ChatColor.YELLOW + AltChar.listDash + "右键单击重置");
		successItemMeta.setLore(successItemLore);
		successItem.setItemMeta(successItemMeta);
		inv.setItem(24, successItem);

		if (success > 0 && !template.getType().corresponds(Type.CONSUMABLE)) {
			ItemStack destroyOnFail = new ItemStack(Material.FISHING_ROD);
			ItemMeta destroyOnFailMeta = destroyOnFail.getItemMeta();
			((Damageable) destroyOnFailMeta).setDamage(30);
			destroyOnFailMeta.setDisplayName(ChatColor.GREEN + "失败时销毁?");
			List<String> destroyOnFailLore = new ArrayList<>();
			destroyOnFailLore.add(ChatColor.GRAY + "开启后, 升级失败");
			destroyOnFailLore.add(ChatColor.GRAY + "时该物品将被销毁");
			destroyOnFailLore.add("");
			destroyOnFailLore.add(ChatColor.GRAY + "当前值: " + ChatColor.GOLD + getEditedSection().getBoolean("upgrade.destroy"));
			destroyOnFailLore.add("");
			destroyOnFailLore.add(ChatColor.YELLOW + AltChar.listDash + "左键单击进行选择");
			destroyOnFailMeta.setLore(destroyOnFailLore);
			destroyOnFail.setItemMeta(destroyOnFailMeta);
			inv.setItem(42, destroyOnFail);
		}

		addEditionInventoryItems(inv, true);

		return inv;
	}

	@Override
	public void whenClicked(InventoryClickEvent event) {
		ItemStack item = event.getCurrentItem();

		event.setCancelled(true);
		if (event.getInventory() != event.getClickedInventory() || !MMOUtils.isMetaItem(item, false))
			return;

		if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "成功机率")) {
			if (event.getAction() == InventoryAction.PICKUP_ALL)
				new StatEdition(this, ItemStats.UPGRADE, "rate").enable("在聊天中写下您想要的成功率");

			if (event.getAction() == InventoryAction.PICKUP_HALF && getEditedSection().contains("upgrade.success")) {
				getEditedSection().set("upgrade.success", null);
				registerTemplateEdition();
				player.sendMessage(MMOItems.plugin.getPrefix() + "成功重置成功几率");
			}
		}

		if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "最大升级数")) {
			if (event.getAction() == InventoryAction.PICKUP_ALL)
				new StatEdition(this, ItemStats.UPGRADE, "max").enable("在聊天中写下您想要的次数");

			if (event.getAction() == InventoryAction.PICKUP_HALF && getEditedSection().contains("upgrade.max")) {
				getEditedSection().set("upgrade.max", null);
				registerTemplateEdition();
				player.sendMessage(MMOItems.plugin.getPrefix() + "成功重置最大升级次数");
			}
		}

		if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "最小升级次数")) {
			if (event.getAction() == InventoryAction.PICKUP_ALL)
				new StatEdition(this, ItemStats.UPGRADE, "min").enable("在聊天中写下您想要的次数");

			if (event.getAction() == InventoryAction.PICKUP_HALF && getEditedSection().contains("upgrade.min")) {
				getEditedSection().set("upgrade.min", null);
				registerTemplateEdition();
				player.sendMessage(MMOItems.plugin.getPrefix() + "成功重置最小升级次数");
			}
		}

		if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "升级模板")) {
			if (event.getAction() == InventoryAction.PICKUP_ALL)
				new StatEdition(this, ItemStats.UPGRADE, "template").enable("在聊天中写下您想要的升级模板ID");

			if (event.getAction() == InventoryAction.PICKUP_HALF && getEditedSection().contains("upgrade.template")) {
				getEditedSection().set("upgrade.template", null);
				registerTemplateEdition();
				player.sendMessage(MMOItems.plugin.getPrefix() + "成功重置升级模板");
			}
		}

		if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "升级参考")) {
			if (event.getAction() == InventoryAction.PICKUP_ALL)
				new StatEdition(this, ItemStats.UPGRADE, "ref").enable("在聊天中写下您想要的升级参考 (文本 ) ");

			if (event.getAction() == InventoryAction.PICKUP_HALF && getEditedSection().contains("upgrade.reference")) {
				getEditedSection().set("upgrade.reference", null);
				registerTemplateEdition();
				player.sendMessage(MMOItems.plugin.getPrefix() + "成功重置升级参考");
			}
		}

		if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "仅工作台升级?")) {
			boolean bool = !getEditedSection().getBoolean("upgrade.workbench");
			getEditedSection().set("upgrade.workbench", bool);
			registerTemplateEdition();
			player.sendMessage(MMOItems.plugin.getPrefix()
					+ (bool ? "您的物品现在必须通过合成表升级" : "您的物品现在可以使用消耗品进行升级"));
		}

		if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "失败时销毁?")) {
			boolean bool = !getEditedSection().getBoolean("upgrade.destroy");
			getEditedSection().set("upgrade.destroy", bool);
			registerTemplateEdition();
			player.sendMessage(MMOItems.plugin.getPrefix()
					+ (bool ? "升级失败后您的物品将被销毁" : "升级失败时您的物品不会被破坏"));
		}
	}
}