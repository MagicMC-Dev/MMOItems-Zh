package net.Indyuce.mmoitems.stat.type;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.util.AltChar;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.item.NBTItem;

public class StringStat extends ItemStat {
	public StringStat(String id, ItemStack item, String name, String[] lore, String[] types, Material... materials) {
		super(id, item, name, lore, types, materials);
	}

	@Override
	public StatData whenInitialized(MMOItem item, Object object) {
		return new StringData(object.toString());
	}

	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		item.addItemTag(new ItemTag(getNBTPath(), data.toString()));
		item.getLore().insert(getPath(), data.toString());
		return true;
	}

	@Override
	public boolean whenClicked(EditionInventory inv, InventoryClickEvent event) {
		ConfigFile config = inv.getItemType().getConfigFile();
		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			config.getConfig().set(inv.getItemId() + "." + getPath(), null);
			inv.registerItemEdition(config);
			inv.open();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed " + getName() + ".");
			return true;
		}
		new StatEdition(inv, this).enable("Write in the chat the text you want.");
		return true;
	}

	@Override
	public boolean whenInput(EditionInventory inv, ConfigFile config, String message, Object... info) {
		config.getConfig().set(inv.getItemId() + "." + getPath(), message);
		inv.registerItemEdition(config);
		inv.open();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + getName() + " successfully changed to " + message + ".");
		return true;
	}

	@Override
	public void whenLoaded(MMOItem mmoitem, NBTItem item) {
		if (item.hasTag(getNBTPath()))
			mmoitem.setData(this, new StringData(item.getString(getNBTPath())));
	}

	@Override
	public void whenDisplayed(List<String> lore, FileConfiguration config, String id) {
		lore.add("");
		if (!config.getConfigurationSection(id).contains(getPath())) {
			lore.add(ChatColor.GRAY + "Current Value:");
			lore.add(ChatColor.RED + "No value.");
		} else {
			String value = ChatColor.translateAlternateColorCodes('&', config.getString(id + "." + getPath()));
			value = value.length() > 40 ? value.substring(0, 40) + "..." : value;
			lore.add(ChatColor.GRAY + "Current Value: " + ChatColor.GREEN + value);
		}
		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Left click to change this value.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove this value.");
	}

	public class StringData implements StatData {
		protected String str;

		public StringData(String str) {
			this.str = str;
		}

		public void setString(String str) {
			this.str = str;
		}

		@Override
		public String toString() {
			return str;
		}
	}
}
