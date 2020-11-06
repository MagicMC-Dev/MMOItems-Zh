package net.Indyuce.mmoitems.stat;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.StringStat;
import net.mmogroup.mmolib.api.item.ItemTag;

public class LoreFormat extends StringStat {
	public LoreFormat() {
		super("LORE_FORMAT", new ItemStack(Material.MAP), "Lore Format", new String[] { "The lore format decides",
				"where each stat goes.", "&9Formats can be configured in", "&9the lore-formats folder" },
				new String[] { "all" });
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
		String path = data.toString();
		Validate.isTrue(MMOItems.plugin.getFormats().hasFormat(path), "Could not find lore format with ID '" + path + "'");

		item.addItemTag(new ItemTag(getNBTPath(), path));
	}

	@Override
	public void whenInput(EditionInventory inv, String message, Object... info) {
		String format = message;
		Validate.isTrue(MMOItems.plugin.getFormats().hasFormat(format), "Couldn't find lore format with ID '" + format + "'.");

		inv.getEditedSection().set(getPath(), format);
		inv.registerTemplateEdition();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Lore Format successfully changed to " + format + ".");
	}
}
