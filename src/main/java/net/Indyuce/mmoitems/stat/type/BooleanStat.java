package net.Indyuce.mmoitems.stat.type;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.util.AltChar;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.item.NBTItem;

public class BooleanStat extends ItemStat {
	public BooleanStat(ItemStack item, String name, String[] lore, String path, String[] types, Material... materials) {
		super(item, name, lore, path, types, materials);
	}

	@Override
	public boolean whenLoaded(MMOItem item, ConfigurationSection config) {
		item.setData(this, new BooleanData(config.getBoolean(getPath())));
		return true;
	}

	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		if (((BooleanData) data).isEnabled()) {
			item.addItemTag(new ItemTag(getNBTPath(), true));
			item.getLore().insert(getPath(), translate());
		}
		return true;
	}

	@Override
	public boolean whenClicked(EditionInventory inv, InventoryClickEvent event) {
		ConfigFile config = inv.getItemType().getConfigFile();
		config.getConfig().set(inv.getItemId() + "." + getPath(), !config.getConfig().getBoolean(inv.getItemId() + "." + getPath()));
		inv.registerItemEdition(config);
		inv.open();
		return true;
	}

	@Override
	public boolean whenInput(EditionInventory inv, ConfigFile config, String message, Object... info) {
		return true;
	}

	@Override
	public void whenLoaded(MMOItem mmoitem, NBTItem item) {
		if (item.hasTag(getNBTPath()))
			mmoitem.setData(this, new BooleanData(item.getBoolean(getNBTPath())));
	}

	@Override
	public void whenDisplayed(List<String> lore, FileConfiguration config, String id) {
		lore.add("");
		lore.add(ChatColor.GRAY + "Current Value: " + (config.getBoolean(id + "." + getPath()) ? ChatColor.GREEN + "true" : ChatColor.RED + "false"));
		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to switch this value.");
	}

	public class BooleanData extends StatData {
		private boolean state;

		public BooleanData(boolean state) {
			this.state = state;
		}

		public boolean isEnabled() {
			return state;
		}

		public void setEnabled(boolean value) {
			state = value;
		}

		@Override
		public String toString() {
			return "" + state;
		}
	}
}
