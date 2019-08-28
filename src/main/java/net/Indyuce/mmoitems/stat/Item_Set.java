package net.Indyuce.mmoitems.stat;

import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.ItemSet;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.StringStat;
import net.Indyuce.mmoitems.version.nms.ItemTag;

public class Item_Set extends StringStat {
	public Item_Set() {
		super(new ItemStack(Material.LEATHER_CHESTPLATE), "Item Set", new String[] { "Item sets can give to the player extra", "bonuses that depend on how many items", "from the same set your wear." }, "set", new String[] { "!gem_stone", "!consumable", "!material", "!miscellaneous", "all" });
	}

	public boolean whenClicked(EditionInventory inv, InventoryClickEvent event, Player player, Type type, String path) {
		if (event.getAction() == InventoryAction.PICKUP_ALL) {
			new StatEdition(inv, ItemStat.SET).enable("Write in the chat the item set ID.");
			player.sendMessage("");
			for (ItemSet set : MMOItems.plugin.getSets().getAll())
				player.sendMessage(ChatColor.GRAY + "* " + ChatColor.GREEN + set.getID() + ChatColor.GRAY + " (" + set.getName() + ChatColor.GRAY + ")");
			return true;
		}

		return true;
	}

	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		String path = data.toString();

		// do not send an error otherwise previously
		// generated items will break
		ItemSet set = MMOItems.plugin.getSets().get(path);
		if (set == null) {
			item.getMMOItem().log(Level.WARNING, "[Item Set] Couldn't find the item set named " + path + ".");
			return false;
		}

		item.addItemTag(new ItemTag("MMOITEMS_ITEM_SET", path));
		item.getLore().insert("set", set.getLoreTag());
		return true;
	}

	@Override
	public void whenLoaded(MMOItem mmoitem, NBTItem item) {
		if (item.hasTag("MMOITEMS_ITEM_SET"))
			mmoitem.setData(this, new StringData(item.getString("MMOITEMS_ITEM_SET")));
	}

	public boolean whenInput(EditionInventory inv, ConfigFile config, String message, Object... info) {
		String format = message.toUpperCase().replace(" ", "_").replace("-", "_");

		ItemSet set = MMOItems.plugin.getSets().get(format);
		if (set == null) {
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "Couldn't find the set named " + format + ".");
			return false;
		}

		config.getConfig().set(inv.getItemId() + ".set", format);
		inv.registerItemEdition(config);
		inv.open();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Set successfully changed to " + set.getName() + ChatColor.GRAY + ".");
		return true;
	}
}
