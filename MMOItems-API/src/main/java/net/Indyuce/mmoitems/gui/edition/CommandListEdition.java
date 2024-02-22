package net.Indyuce.mmoitems.gui.edition;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.util.MMOUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class CommandListEdition extends EditionInventory {
	private static final int[] slots = { 19, 20, 21, 22, 23, 24, 25, 28, 29, 33, 34, 37, 38, 42, 43 };

	public CommandListEdition(Player player, MMOItemTemplate template) {
		super(player, template);
	}

	@Override
	public String getName() {
		return "命令列表";
	}

	@Override
	public void arrangeInventory() {
		int n = 0;

		if (getEditedSection().contains("commands"))
			for (String key : getEditedSection().getConfigurationSection("commands").getKeys(false)) {

				String format = getEditedSection().getString("commands." + key + ".format");
				double delay = getEditedSection().getDouble("commands." + key + ".delay");
				boolean console = getEditedSection().getBoolean("commands." + key + ".console"),
						op = getEditedSection().getBoolean("commands." + key + ".op");

				ItemStack item = new ItemStack(VersionMaterial.COMPARATOR.toMaterial());
				ItemMeta itemMeta = item.getItemMeta();
				itemMeta.setDisplayName(format == null || format.equals("") ? ChatColor.RED + "无格式" : ChatColor.GREEN + format);
				List<String> itemLore = new ArrayList<>();
				itemLore.add("");
				itemLore.add(ChatColor.GRAY + "命令延迟: " + ChatColor.RED + delay);
				itemLore.add(ChatColor.GRAY + "由控制台发送: " + ChatColor.RED + console);
				itemLore.add(ChatColor.GRAY + "发送时带有 OP 权限: " + ChatColor.RED + op);
				itemLore.add("");
				itemLore.add(ChatColor.YELLOW + AltChar.listDash + " 右键单击即可删除");
				itemMeta.setLore(itemLore);
				item.setItemMeta(itemMeta);

				inventory.setItem(slots[n++], NBTItem.get(item).addTag(new ItemTag("configKey", key)).toItem());
			}

		ItemStack glass = VersionMaterial.GRAY_STAINED_GLASS_PANE.toItem();
		ItemMeta glassMeta = glass.getItemMeta();
		glassMeta.setDisplayName(ChatColor.RED + "- 无命令 -");
		glass.setItemMeta(glassMeta);

		ItemStack add = new ItemStack(VersionMaterial.WRITABLE_BOOK.toMaterial());
		ItemMeta addMeta = add.getItemMeta();
		addMeta.setDisplayName(ChatColor.GREEN + "创建一个命令...");
		add.setItemMeta(addMeta);

		inventory.setItem(40, add);
		while (n < slots.length)
			inventory.setItem(slots[n++], glass);
	}

	@Override
	public void whenClicked(InventoryClickEvent event) {
		ItemStack item = event.getCurrentItem();

		event.setCancelled(true);
		if (event.getInventory() != event.getClickedInventory() || !MMOUtils.isMetaItem(item, false))
			return;

		if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "创建一个命令...")) {
			new StatEdition(this, ItemStats.COMMANDS).enable("在聊天中输入您要添加的命令", "", "要添加延迟, 请使用" + ChatColor.RED + "-d:<delay>",
					"要使命令通过控制台自行转换, 请使用" + ChatColor.RED + "-c", "要使命令具有 OP 权限, 请使用" + ChatColor.RED + "-op", "",
					ChatColor.YELLOW + "例如: -d:10.3 -op bc Hello, 这是一个测试命令");
			return;
		}

		String tag = MythicLib.plugin.getVersion().getWrapper().getNBTItem(item).getString("configKey");
		if (tag.equals(""))
			return;

		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			if (getEditedSection().contains("commands") && getEditedSection().getConfigurationSection("commands").contains(tag)) {
				getEditedSection().set("commands." + tag, null);
				registerTemplateEdition();
				player.sendMessage(MMOItems.plugin.getPrefix() + "已成功删除" + ChatColor.GOLD + tag + ChatColor.DARK_GRAY
						+ "(内部ID)" + ChatColor.GRAY + ".");
			}
		}
	}
}