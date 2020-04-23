package net.Indyuce.mmoitems.stat;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.itemgen.RandomStatData;
import net.mmogroup.mmolib.api.util.AltChar;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StringListData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.item.NBTItem;

public class NBTTags extends ItemStat {
	public NBTTags() {
		super("CUSTOM_NBT", new ItemStack(Material.NAME_TAG), "NBT Tags", new String[] { "Custom NBT Tags." }, new String[] { "all" });
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
			new StatEdition(inv, ItemStat.NBT_TAGS).enable("Write in the chat the NBT tag you want to add.",
					ChatColor.AQUA + "Format: [TAG_NAME] [TAG_VALUE]");

		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			if (config.getConfig().getConfigurationSection(inv.getItemId()).contains("custom-nbt")) {
				List<String> nbtTags = config.getConfig().getStringList(inv.getItemId() + ".custom-nbt");
				if (nbtTags.size() < 1)
					return true;

				String last = nbtTags.get(nbtTags.size() - 1);
				nbtTags.remove(last);
				config.getConfig().set(inv.getItemId() + ".custom-nbt", nbtTags);
				inv.registerItemEdition(config);
				inv.open();
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed '" + ChatColor.translateAlternateColorCodes('&', last)
						+ ChatColor.GRAY + "'.");
			}
		}
		return true;
	}

	@Override
	public boolean whenInput(EditionInventory inv, ConfigFile config, String message, Object... info) {
		if (message.split("\\ ").length < 2) {
			inv.getPlayer().sendMessage(ChatColor.RED + "Invalid format");
			return false;
		}

		List<String> customNbt = config.getConfig().getConfigurationSection(inv.getItemId()).contains("custom-nbt")
				? config.getConfig().getStringList(inv.getItemId() + ".custom-nbt")
				: new ArrayList<>();
		customNbt.add(message);
		config.getConfig().set(inv.getItemId() + ".custom-nbt", customNbt);
		inv.registerItemEdition(config);
		inv.open();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "StringListStat successfully added.");
		return true;
	}

	@Override
	public void whenDisplayed(List<String> lore, FileConfiguration config, String path) {
		lore.add("");
		lore.add(ChatColor.GRAY + "Current Value:");
		if (!config.getConfigurationSection(path).contains("custom-nbt"))
			lore.add(ChatColor.RED + "No NBT Tags.");
		else if (config.getStringList(path + ".custom-nbt").isEmpty())
			lore.add(ChatColor.RED + "No NBT Tags.");
		else
			config.getStringList(path + ".custom-nbt").forEach(str -> lore.add(ChatColor.GRAY + ChatColor.translateAlternateColorCodes('&', str)));
		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to add a tag.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove the last tag.");
	}

	@Override
	public void whenApplied(MMOItemBuilder item, StatData data) {
		JsonArray array = new JsonArray();
		((StringListData) data).getList().forEach(tag -> {
			array.add(tag);

			item.addItemTag(new ItemTag(tag.substring(0, tag.indexOf(' ')), tag.substring(tag.indexOf(' ') + 1)));
		});
		item.addItemTag(new ItemTag("MMOITEMS_NBTTAGS", array.toString()));
	}

	@Override
	public void whenLoaded(MMOItem mmoitem, NBTItem item) {
		if (item.hasTag("MMOITEMS_NBTTAGS"))
			mmoitem.setData(ItemStat.NBT_TAGS, new StringListData(new JsonParser().parse(item.getString("MMOITEMS_NBTTAGS")).getAsJsonArray()));
	}
}
