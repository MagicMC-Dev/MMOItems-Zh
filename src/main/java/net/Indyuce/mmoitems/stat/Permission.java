package net.Indyuce.mmoitems.stat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StringListData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemRestriction;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.ProperStat;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.item.NBTItem;
import net.mmogroup.mmolib.api.util.AltChar;
import net.mmogroup.mmolib.version.VersionMaterial;

public class Permission extends ItemStat implements ItemRestriction, ProperStat {
	public Permission() {
		super("PERMISSION", new ItemStack(VersionMaterial.OAK_SIGN.toMaterial()), "Permission",
				new String[] { "The permission needed to use this item." }, new String[] { "!block", "all" });
	}

	@Override
	@SuppressWarnings("unchecked")
	public StringListData whenInitialized(Object object) {
		Validate.isTrue(object instanceof List<?>, "Must specify a string list");
		return new StringListData((List<String>) object);
	}

	@Override
	public void whenClicked(EditionInventory inv, InventoryClickEvent event) {
		ConfigFile config = inv.getEdited().getType().getConfigFile();
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new StatEdition(inv, ItemStat.PERMISSION).enable("Write in the chat the permission you want your item to require.");

		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			if (config.getConfig().getConfigurationSection(inv.getEdited().getId()).contains("permission")) {
				List<String> requiredPerms = config.getConfig().getStringList(inv.getEdited().getId() + ".permission");
				if (requiredPerms.size() < 1)
					return;

				String last = requiredPerms.get(requiredPerms.size() - 1);
				requiredPerms.remove(last);
				config.getConfig().set(inv.getEdited().getId() + ".permission", requiredPerms.size() == 0 ? null : requiredPerms);
				inv.registerTemplateEdition(config);
				inv.open();
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed " + last + ".");
			}
		}
	}

	@Override
	public boolean whenInput(EditionInventory inv, ConfigFile config, String message, Object... info) {
		if (message.contains("|")) {
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Your perm node must not contain any | symbol.");
			return false;
		}

		List<String> lore = config.getConfig().getConfigurationSection(inv.getEdited().getId()).contains("permission")
				? config.getConfig().getStringList(inv.getEdited().getId() + ".permission")
				: new ArrayList<>();
		lore.add(message);
		config.getConfig().set(inv.getEdited().getId() + ".permission", lore);
		inv.registerTemplateEdition(config);
		inv.open();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Permission successfully added.");
		return true;
	}

	@Override
	public void whenDisplayed(List<String> lore, MMOItem mmoitem) {

		if (mmoitem.hasData(this)) {
			lore.add(ChatColor.GRAY + "Current Value:");
			StringListData data = (StringListData) mmoitem.getData(this);
			data.getList().forEach(el -> lore.add(ChatColor.GRAY + "* " + ChatColor.GREEN + el));

		} else
			lore.add(ChatColor.GRAY + "Current Value: " + ChatColor.RED + "None");

		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to add a compatible permission.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove the last permission.");
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
		item.addItemTag(new ItemTag("MMOITEMS_PERMISSION", String.join("|", ((StringListData) data).getList())));
	}

	@Override
	public void whenLoaded(ReadMMOItem mmoitem) {
		if (mmoitem.getNBT().hasTag(getNBTPath()))
			mmoitem.setData(this, new StringListData(Arrays.asList(mmoitem.getNBT().getString(getNBTPath()).split("\\|"))));
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
