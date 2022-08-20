package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.MaterialData;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import net.Indyuce.mmoitems.stat.type.ItemRestriction;
import net.Indyuce.mmoitems.stat.type.Upgradable;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;

/**
 * See {@link CustomDurability} for useful comments
 *
 * @author indyuce
 */
public class MaximumDurability extends DoubleStat implements ItemRestriction, GemStoneStat, Upgradable {
	public MaximumDurability() {
		super("MAX_DURABILITY", Material.SHEARS, "Maximum Durability", new String[]{"The amount of uses before your", "item becomes unusable/breaks."}, new String[]{"!block", "all"});
	}

	@Override
	public void whenPreviewed(@NotNull ItemStackBuilder item, @NotNull DoubleData currentData, @NotNull NumericStatFormula templateData) throws IllegalArgumentException {
		whenApplied(item, currentData);
	}

	@Override
	public void whenApplied(@NotNull ItemStackBuilder item, @NotNull DoubleData data) {

		int max = (int) data.getValue();
		int current = item.getMMOItem().hasData(ItemStats.CUSTOM_DURABILITY) ? (int) ((DoubleData) item.getMMOItem().getData(ItemStats.CUSTOM_DURABILITY)).getValue() : max;

		item.addItemTag(new ItemTag(getNBTPath(), max));

		// Display durability in lore here.
		String format = MMOItems.plugin.getLanguage().getStatFormat("durability").replace("{max}", String.valueOf(max)).replace("{current}", String.valueOf(current));
		item.getLore().insert("durability", format);
	}

	@Override
	public void preprocess(@NotNull MMOItem item) {

		// If this has no Max Upgrade Data
		if (!item.hasData(ItemStats.MAX_DURABILITY)) {

			// What durability will it have?
			int base = 400;

			// I mean bruh
			if (item.hasData(ItemStats.MATERIAL)) {

				// Use vanilla max durability
				MaterialData data = (MaterialData) item.getData(ItemStats.MATERIAL);

				// Get mat
				Material mat = data.getMaterial();
				base = mat.getMaxDurability();
			}

			// Yea no
			if (base < 8)
				base = 400;

			// Set max dura
			item.setData(ItemStats.MAX_DURABILITY, new DoubleData(base));
			/*item.setData(ItemStats.CUSTOM_DURABILITY, new DoubleData(base));*/
		}
	}

	@Override
	public boolean canUse(RPGPlayer player, NBTItem item, boolean message) {

		/*
		 * Items with no MMOITEMS_DURABILITY tag yet means that they still
		 * have full durability
		 */
		if (!item.hasTag("MMOITEMS_DURABILITY"))
			return true;

		if (item.getDouble(ItemStats.CUSTOM_DURABILITY.getNBTPath()) <= 0) {
			if (message) {
				Message.ZERO_DURABILITY.format(ChatColor.RED).send(player.getPlayer());
				player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1.5f);
			}
			return false;
		}
		return true;
	}
}
