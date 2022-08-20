package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.StringStat;
import io.lumine.mythic.lib.api.item.ItemTag;
import org.jetbrains.annotations.NotNull;

public class ItemTierStat extends StringStat implements GemStoneStat {
	public ItemTierStat() {
		super("TIER", Material.DIAMOND, "Item Tier", new String[] { "The tier defines how rare your item is", "and what item is dropped when your",
				"item is deconstructed.", "&9Tiers can be configured in the tiers.yml file" }, new String[] { "all" });
	}

	@Override
	public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StringData data) {
		String path = data.toString().toUpperCase().replace("-", "_").replace(" ", "_");
		Validate.isTrue(MMOItems.plugin.getTiers().has(path), "Could not find item tier with ID '" + path + "'");

		ItemTier tier = MMOItems.plugin.getTiers().get(path);
		item.addItemTag(new ItemTag("MMOITEMS_TIER", path));
		item.getLore().insert("tier", MMOItems.plugin.getLanguage().getStatFormat(getPath()).replace("{value}", tier.getName()));
	}

	@Override
	public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
		String format = message.toUpperCase().replace(" ", "_").replace("-", "_");
		Validate.isTrue(MMOItems.plugin.getTiers().has(format), "Couldn't find the tier called '" + format + "'.");

		inv.getEditedSection().set("tier", format);
		inv.registerTemplateEdition();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Tier successfully changed to " + format + ".");
	}
}
