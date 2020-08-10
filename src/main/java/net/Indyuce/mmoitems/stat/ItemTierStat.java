package net.Indyuce.mmoitems.stat;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.StringStat;
import net.mmogroup.mmolib.api.item.ItemTag;

public class ItemTierStat extends StringStat {
	public ItemTierStat() {
		super("TIER", new ItemStack(Material.DIAMOND), "Item Tier", new String[] { "The tier defines how rare your item is",
				"and what item is dropped when your", "item is deconstructed.", "&9Tiers can be configured in the tiers.yml file" },
				new String[] { "all" });
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
		String path = data.toString().toUpperCase().replace("-", "_").replace(" ", "_");
		Validate.isTrue(MMOItems.plugin.getTiers().has(path), "Could not find item tier with ID '" + path + "'");

		ItemTier tier = MMOItems.plugin.getTiers().get(path);
		item.addItemTag(new ItemTag("MMOITEMS_TIER", path));
		item.getLore().insert("tier", MMOItems.plugin.getLanguage().getStatFormat(getPath()).replace("#", tier.getName()));
	}

	@Override
	public boolean whenInput(EditionInventory inv, ConfigFile config, String message, Object... info) {
		String format = message.toUpperCase().replace(" ", "_").replace("-", "_");

		if (!MMOItems.plugin.getTiers().has(format)) {
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "Couldn't find the tier called " + format + ".");
			return false;
		}

		config.getConfig().set(inv.getEdited().getId() + ".tier", format);
		inv.registerTemplateEdition(config);
		inv.open();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Tier successfully changed to " + format + ".");
		return true;
	}
}
