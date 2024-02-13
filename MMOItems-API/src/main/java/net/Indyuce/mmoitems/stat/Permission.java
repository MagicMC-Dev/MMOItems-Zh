package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StringListData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemRestriction;
import net.Indyuce.mmoitems.stat.type.StringListStat;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Permission extends StringListStat implements ItemRestriction {
	public Permission() {
		super("PERMISSION", VersionMaterial.OAK_SIGN.toMaterial(), "权限",
				new String[] { "使用该物品所需的权限" }, new String[] { "!block", "all" });
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
			new StatEdition(inv, ItemStats.PERMISSION).enable("在聊天中输入您希望您的物品需要的权限");

		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			if (inv.getEditedSection().contains("permission")) {
				List<String> requiredPerms = inv.getEditedSection().getStringList("permission");
				if (requiredPerms.size() < 1)
					return;

				String last = requiredPerms.get(requiredPerms.size() - 1);
				requiredPerms.remove(last);
				inv.getEditedSection().set("permission", requiredPerms.size() == 0 ? null : requiredPerms);
				inv.registerTemplateEdition();
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "已成功删除" + last + ".");
			}
		}
	}

	@Override
	public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
		Validate.isTrue(!message.contains("|"), "您的权限节点不得包含任何 | symbol字符");
		List<String> lore = inv.getEditedSection().contains("permission") ? inv.getEditedSection().getStringList("permission") : new ArrayList<>();
		lore.add(message);
		inv.getEditedSection().set("permission", lore);
		inv.registerTemplateEdition();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "权限添加成功");
	}

	@Override
	public void whenDisplayed(List<String> lore, Optional<StringListData> statData) {

		if (statData.isPresent()) {
			lore.add(ChatColor.GRAY + "Current Value:");
			StringListData data = (StringListData) statData.get();
			data.getList().forEach(el -> lore.add(ChatColor.GRAY + "* " + ChatColor.GREEN + el));

		} else
			lore.add(ChatColor.GRAY + "当前值: " + ChatColor.RED + "None");

		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " 单击以添加兼容权限");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " 右键单击删除最后一个权限");
	}

	@NotNull
	@Override
	public ArrayList<ItemTag> getAppliedNBT(@NotNull StringListData data) {

		// Create Fresh
		ArrayList<ItemTag> ret = new ArrayList<>();

		// Create tag and add
		ret.add(new ItemTag(getNBTPath(), String.join("|", ((StringListData) data).getList())));

		// Return thay
		return ret;
	}

	@Override
	public void whenLoaded(@NotNull ReadMMOItem mmoitem) {

		// Find tags
		ArrayList<ItemTag> revTgs = new ArrayList<>();
		if (mmoitem.getNBT().hasTag(getNBTPath()))
			revTgs.add(ItemTag.getTagAtPath(getNBTPath(), mmoitem.getNBT(), SupportedNBTTagValues.STRING));

		// BUild Data
		StatData data = getLoadedNBT(revTgs);

		// Valid?
		if (data != null) { mmoitem.setData(this, data); }
	}

	@Nullable
	@Override
	public StringListData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {

		// Find relevant tag
		ItemTag encoded = ItemTag.getTagAtPath(getNBTPath(), storedTags);

		// Found it?
		if (encoded != null) {

			// Split and make list
			ArrayList<String> list = new ArrayList<>(Arrays.asList(((String) encoded.getValue()).split("\\|")));

			// Build and return
			return new StringListData(list);
		}

		return null;
	}

	@Override
	public boolean canUse(RPGPlayer player, NBTItem item, boolean message) {
		String perm = item.getString("MMOITEMS_PERMISSION");
		if (!perm.equals("") && !player.getPlayer().hasPermission("mmoitems.bypass.item")
				&& MMOItems.plugin.getConfig().getBoolean("permissions.items")) {
			String[] split = perm.split("\\|");
			for (String s : split)
				if (!player.getPlayer().hasPermission(s)) {
					if (message) {
						Message.NOT_ENOUGH_PERMS.format(ChatColor.RED).send(player.getPlayer());
						player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1.5f);
					}
					return false;
				}
		}
		return true;
	}
}
