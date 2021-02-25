package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackCategory;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackProvider;
import io.lumine.mythic.lib.api.util.ui.PlusMinusPercent;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.util.message.FriendlyFeedbackPalette_MMOItems;
import net.Indyuce.mmoitems.stat.data.MaterialData;
import net.Indyuce.mmoitems.stat.data.type.UpgradeInfo;
import net.Indyuce.mmoitems.stat.type.Upgradable;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;

import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.ItemRestriction;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import org.bukkit.entity.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class MaximumDurability extends DoubleStat implements ItemRestriction, GemStoneStat, Upgradable {
	public MaximumDurability() {
		super("MAX_DURABILITY", Material.SHEARS, "Maximum Durability", new String[] { "The amount of uses before your", "item becomes unusable/breaks." }, new String[] { "!block", "all"});
	}

	/*
	 * initializes the custom durability mecanism on an item.
	 */
	@Override
	public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StatData data) {
		super.whenApplied(item, data);
	}

	@Override
	public @NotNull
	ArrayList<ItemTag> getAppliedNBT(@NotNull StatData data) {

		// Create Fresh
		ArrayList<ItemTag> ret = new ArrayList<>();

		// Add sole tag
		ret.add(new ItemTag(getNBTPath(), ((DoubleData) data).getValue()));
		ret.add(new ItemTag(ItemStats.DURABILITY.getNBTPath(), ((DoubleData) data).getValue()));

		// Return thay
		return ret;
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
			if (base < 8) { base = 400; }

			// Set max dura
			item.setData(ItemStats.MAX_DURABILITY, new DoubleData(base));
			item.setData(ItemStats.DURABILITY, new DoubleData(base));
		}
	}

	@Override
	public boolean canUse(RPGPlayer player, NBTItem item, boolean message) {

		// No max durability not MMOItems' problem
		if (!item.hasTag(ItemStats.MAX_DURABILITY.getNBTPath())) { return true; }

		// No durability? Uuuuh that's weird but ok
		if (!item.hasTag(ItemStats.DURABILITY.getNBTPath())) {

			// Initialize to max durability and roll
			item.addTag(new ItemTag(ItemStats.DURABILITY.getNBTPath(), item.getDouble(ItemStats.MAX_DURABILITY.getNBTPath())));
		}

		if (item.getDouble(ItemStats.DURABILITY.getNBTPath()) <= 0) {
			if (message) {
				Message.ZERO_DURABILITY.format(ChatColor.RED).send(player.getPlayer(), "cant-use-item");
				player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1.5f); }
			return false;
		}
		return true;
	}

	@NotNull
	@Override
	public UpgradeInfo loadUpgradeInfo(@Nullable Object obj) throws IllegalArgumentException {

		// Return result of thay
		return DoubleStat.DoubleUpgradeInfo.GetFrom(obj);
	}
}
