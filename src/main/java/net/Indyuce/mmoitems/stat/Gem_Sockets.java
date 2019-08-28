package net.Indyuce.mmoitems.stat;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.util.AltChar;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.GemSocketsData;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.Indyuce.mmoitems.stat.data.StringListData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.version.nms.ItemTag;

public class Gem_Sockets extends ItemStat {
	public Gem_Sockets() {
		super(new ItemStack(Material.EMERALD), "Gem Sockets", new String[] { "The amount of gem", "sockets your weapon has." }, "gem-sockets", new String[] { "piercing", "slashing", "blunt", "offhand", "range", "tool", "armor" });
	}

	public boolean whenLoaded(MMOItem item, ConfigurationSection config) {
		item.setData(this, new GemSocketsData(new StringListData(config.getStringList("gem-sockets"))));
		return true;
	}

	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		GemSocketsData sockets = (GemSocketsData) data;
		item.addItemTag(new ItemTag("MMOITEMS_GEM_STONES", sockets.toJson().toString()));

		String empty = ItemStat.translate("empty-gem-socket"), filled = ItemStat.translate("filled-gem-socket");

		List<String> lore = new ArrayList<>();
		sockets.getGemstones().forEach(gem -> lore.add(filled.replace("#", gem.getName())));
		sockets.getEmptySlots().forEach(slot -> lore.add(empty.replace("#", slot)));
		item.getLore().insert("gem-stones", lore);

		return true;
	}

	@Override
	public void whenLoaded(MMOItem mmoitem, NBTItem item) {
		if (item.hasTag("MMOITEMS_GEM_STONES"))
			try {
				JsonObject object = new JsonParser().parse(item.getString("MMOITEMS_GEM_STONES")).getAsJsonObject();
				GemSocketsData sockets = new GemSocketsData(toList(object.getAsJsonArray("EmptySlots")));

				JsonArray array = object.getAsJsonArray("Gemstones");
				array.forEach(element -> sockets.add(sockets.newGemstone(element.getAsJsonObject())));

				mmoitem.setData(this, sockets);
			} catch (JsonSyntaxException exception) {
				/*
				 * OLD ITEM WHICH MUST BE UPDATED.
				 */
			}
	}

	private List<String> toList(JsonArray array) {
		List<String> list = new ArrayList<>();
		array.forEach(str -> list.add(str.getAsString()));
		return list;
	}

	@Override
	public boolean whenClicked(EditionInventory inv, InventoryClickEvent event) {
		ConfigFile config = inv.getItemType().getConfigFile();
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new StatEdition(inv, ItemStat.GEM_SOCKETS).enable("Write in the chat the COLOR of the gem socket you want to add.");

		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			if (config.getConfig().getConfigurationSection(inv.getItemId()).contains(getPath())) {
				List<String> lore = config.getConfig().getStringList(inv.getItemId() + "." + getPath());
				if (lore.size() < 1)
					return true;

				String last = lore.get(lore.size() - 1);
				lore.remove(last);
				config.getConfig().set(inv.getItemId() + "." + getPath(), lore);
				inv.registerItemEdition(config);
				inv.open();
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed '" + ChatColor.translateAlternateColorCodes('&', last) + ChatColor.GRAY + "'.");
			}
		}
		return true;
	}

	@Override
	public boolean whenInput(EditionInventory inv, ConfigFile config, String message, Object... info) {
		List<String> lore = config.getConfig().getConfigurationSection(inv.getItemId()).contains(getPath()) ? config.getConfig().getStringList(inv.getItemId() + "." + getPath()) : new ArrayList<>();
		lore.add(message);
		config.getConfig().set(inv.getItemId() + "." + getPath(), lore);
		inv.registerItemEdition(config);
		inv.open();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + message + " successfully added.");
		return true;
	}

	@Override
	public void whenDisplayed(List<String> lore, FileConfiguration config, String path) {
		lore.add("");
		lore.add(ChatColor.GRAY + "Current Value:");
		if (!config.getConfigurationSection(path).contains("gem-sockets"))
			lore.add(ChatColor.RED + "No sockets.");
		else if (config.getStringList(path + ".gem-sockets").isEmpty())
			lore.add(ChatColor.RED + "No sockets.");
		else
			for (String s1 : config.getStringList(path + ".gem-sockets"))
				lore.add(ChatColor.GRAY + "* " + ChatColor.GREEN + s1 + " Gem Socket");
		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to add a gem socket.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove the socket.");
	}
}
