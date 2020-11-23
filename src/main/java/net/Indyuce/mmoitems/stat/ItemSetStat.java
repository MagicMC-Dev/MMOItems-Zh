package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemSet;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.StringStat;
import net.mmogroup.mmolib.api.item.ItemTag;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ItemSetStat extends StringStat {
	public ItemSetStat() {
		super("SET", new ItemStack(Material.LEATHER_CHESTPLATE), "Item Set",
				new String[] { "Item sets can give to the player extra", "bonuses that depend on how many items", "from the same set your wear." },
				new String[] { "!gem_stone", "!consumable", "!material", "!block", "!miscellaneous", "all" });
	}

	@Override
	public void whenClicked(EditionInventory inv, InventoryClickEvent e) {
		super.whenClicked(inv, e);
		if (e.getAction() != InventoryAction.PICKUP_HALF) {
			inv.getPlayer().sendMessage(ChatColor.GREEN + "Available Item Sets:");
			StringBuilder builder = new StringBuilder();
			for (ItemSet set : MMOItems.plugin.getSets().getAll())
				builder.append(ChatColor.GREEN).append(set.getId()).append(ChatColor.GRAY)
					.append(" (").append(set.getName()).append(ChatColor.GRAY).append("), ");
			if(builder.length() > 1)
				builder.setLength(builder.length() - 2);
			inv.getPlayer().sendMessage(builder.toString());
		}
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
		String path = data.toString();

		ItemSet set = MMOItems.plugin.getSets().get(path);
		Validate.notNull(set, "Could not find item set with ID '" + path + "'");

		item.addItemTag(new ItemTag("MMOITEMS_ITEM_SET", path));
		item.getLore().insert("set", set.getLoreTag());
	}

	@Override
	public void whenLoaded(ReadMMOItem mmoitem) {
		if (mmoitem.getNBT().hasTag("MMOITEMS_ITEM_SET"))
			mmoitem.setData(this, new StringData(mmoitem.getNBT().getString("MMOITEMS_ITEM_SET")));
	}

	@Override
	public void whenInput(EditionInventory inv, String message, Object... info) {
		ItemSet set = MMOItems.plugin.getSets().get(message);
		Validate.notNull(set, "Couldn't find the set named '" + message + "'.");
		super.whenInput(inv, message, info);
	}
}
