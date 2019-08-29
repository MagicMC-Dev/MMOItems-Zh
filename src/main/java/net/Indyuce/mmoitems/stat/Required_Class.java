package net.Indyuce.mmoitems.stat;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.Message;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.api.util.AltChar;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.Indyuce.mmoitems.stat.data.StringListData;
import net.Indyuce.mmoitems.stat.type.Conditional;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.StringStat;
import net.Indyuce.mmoitems.version.VersionMaterial;
import net.Indyuce.mmoitems.version.nms.ItemTag;

public class Required_Class extends StringStat implements Conditional {
	public Required_Class() {
		super(new ItemStack(VersionMaterial.WRITABLE_BOOK.toMaterial()), "Required Class", new String[] { "The class you need to", "profress to use your item." }, "required-class", new String[] { "all" });
	}

	@Override
	public boolean whenClicked(EditionInventory inv, InventoryClickEvent event) {
		ConfigFile config = inv.getItemType().getConfigFile();
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new StatEdition(inv, ItemStat.REQUIRED_CLASS).enable("Write in the chat the class you want your item to support.");

		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			if (config.getConfig().getConfigurationSection(inv.getItemId()).getKeys(false).contains("required-class")) {
				List<String> supportedClasses = config.getConfig().getStringList(inv.getItemId() + ".required-class");
				if (supportedClasses.size() < 1)
					return true;

				String last = supportedClasses.get(supportedClasses.size() - 1);
				supportedClasses.remove(last);
				config.getConfig().set(inv.getItemId() + ".required-class", supportedClasses.size() == 0 ? null : supportedClasses);
				inv.registerItemEdition(config);
				inv.open();
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed " + last + ".");
			}
		}
		return true;
	}

	@Override
	public boolean whenInput(EditionInventory inv, ConfigFile config, String message, Object... info) {
		List<String> lore = (config.getConfig().getConfigurationSection(inv.getItemId()).getKeys(false).contains("required-class") ? config.getConfig().getStringList(inv.getItemId() + ".required-class") : new ArrayList<>());
		lore.add(message);
		config.getConfig().set(inv.getItemId() + ".required-class", lore);
		inv.registerItemEdition(config);
		inv.open();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Required Class successfully added.");
		return true;
	}

	@Override
	public void whenLoaded(MMOItem mmoitem, NBTItem item) {
		if (item.hasTag(getNBTPath()))
			mmoitem.setData(this, new StringListData(item.getString(getNBTPath()).split(Pattern.quote(", "))));
	}

	@Override
	public void whenDisplayed(List<String> lore, FileConfiguration config, String path) {
		lore.add("");
		lore.add(ChatColor.GRAY + "Current Value:");
		if (!config.getConfigurationSection(path).contains("required-class"))
			lore.add(ChatColor.RED + "No required class.");
		else
			for (String s : config.getStringList(path + ".required-class"))
				lore.add(ChatColor.GRAY + "* " + ChatColor.GREEN + s);
		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to add a class.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove the last class.");
	}

	@Override
	public boolean whenLoaded(MMOItem item, ConfigurationSection config) {
		item.setData(ItemStat.REQUIRED_CLASS, new StringListData(config.getStringList("required-class")));
		return true;
	}

	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		String joined = String.join(", ", ((StringListData) data).getList());
		item.getLore().insert("required-class", translate().replace("#", joined));
		item.addItemTag(new ItemTag("MMOITEMS_REQUIRED_CLASS", joined));
		return true;
	}

	@Override
	public boolean canUse(RPGPlayer player, NBTItem item, boolean message) {
		String requiredClass = item.getString("MMOITEMS_REQUIRED_CLASS");
		if (!requiredClass.equals("") && !hasRightClass(player, requiredClass) && !player.getPlayer().hasPermission("mmoitems.bypass.class")) {
			if (message) {
				Message.WRONG_CLASS.format(ChatColor.RED).send(player.getPlayer(), "cant-use-item");
				player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1.5f);
			}
			return false;
		}
		return true;
	}

	private boolean hasRightClass(RPGPlayer player, String requiredClass) {
		String name = ChatColor.stripColor(player.getClassName());

		for (String found : requiredClass.split(Pattern.quote(", ")))
			if (found.equalsIgnoreCase(name))
				return true;

		return false;
	}
}
