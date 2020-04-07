package net.Indyuce.mmoitems.stat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.itemgen.RandomStatData;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.api.util.AltChar;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StringListData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemRestriction;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.item.NBTItem;
import net.mmogroup.mmolib.version.VersionMaterial;

public class Permission extends ItemStat implements ItemRestriction {
	public Permission() {
		super("PERMISSION", new ItemStack(VersionMaterial.OAK_SIGN.toMaterial()), "Permission",
				new String[] { "The permission needed to use this item." }, new String[] { "all" });
	}

	@Override
	@SuppressWarnings("unchecked")
	public StringListData whenInitialized(Object object) {
		Validate.isTrue(object instanceof List<?>, "Must specify a string list");
		return new StringListData((List<String>) object);
	}

	@Override
	public RandomStatData whenInitializedGeneration(Object object) {
		return whenInitialized(object);
	}

	@Override
	public boolean whenClicked(EditionInventory inv, InventoryClickEvent event) {
		ConfigFile config = inv.getItemType().getConfigFile();
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new StatEdition(inv, ItemStat.PERMISSION).enable("Write in the chat the permission you want your item to require.");

		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			if (config.getConfig().getConfigurationSection(inv.getItemId()).contains("permission")) {
				List<String> requiredPerms = config.getConfig().getStringList(inv.getItemId() + ".permission");
				if (requiredPerms.size() < 1)
					return true;
				String last = requiredPerms.get(requiredPerms.size() - 1);
				requiredPerms.remove(last);
				config.getConfig().set(inv.getItemId() + ".permission", requiredPerms.size() == 0 ? null : requiredPerms);
				inv.registerItemEdition(config);
				inv.open();
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed " + last + ".");
			}
		}
		return true;
	}

	@Override
	public boolean whenInput(EditionInventory inv, ConfigFile config, String message, Object... info) {
		if (message.contains("|")) {
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Your perm node must not contain any | symbol.");
			return false;
		}

		List<String> lore = config.getConfig().getConfigurationSection(inv.getItemId()).contains("permission")
				? config.getConfig().getStringList(inv.getItemId() + ".permission")
				: new ArrayList<>();
		lore.add(message);
		config.getConfig().set(inv.getItemId() + ".permission", lore);
		inv.registerItemEdition(config);
		inv.open();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Permission successfully added.");
		return true;
	}

	@Override
	public void whenDisplayed(List<String> lore, FileConfiguration config, String path) {
		lore.add("");
		lore.add(ChatColor.GRAY + "Current Value:");
		if (!config.getConfigurationSection(path).contains("permission"))
			lore.add(ChatColor.RED + "No permission.");
		else
			for (String s : config.getStringList(path + ".permission"))
				lore.add(ChatColor.GRAY + "* " + ChatColor.GREEN + s);
		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to add a required permission.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove the last permission.");
	}

	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		item.addItemTag(new ItemTag("MMOITEMS_PERMISSION", String.join("|", ((StringListData) data).getList())));
		return true;
	}

	@Override
	public void whenLoaded(MMOItem mmoitem, NBTItem item) {
		if (item.hasTag(getNBTPath()))
			mmoitem.setData(this, new StringListData(Arrays.asList(item.getString(getNBTPath()).split("\\|"))));
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
						Message.NOT_ENOUGH_PERMS.format(ChatColor.RED).send(player.getPlayer(), "cant-use-item");
						player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1.5f);
					}
					return false;
				}
		}
		return true;
	}
}
