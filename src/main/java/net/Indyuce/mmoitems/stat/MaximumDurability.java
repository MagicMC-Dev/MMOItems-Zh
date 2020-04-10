package net.Indyuce.mmoitems.stat;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemRestriction;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.ProperStat;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.item.NBTItem;

public class MaximumDurability extends DoubleStat implements ItemRestriction, ProperStat {
	public MaximumDurability() {
		super("MAX_DURABILITY", new ItemStack(Material.SHEARS), "Maximum Durability", new String[] { "The amount of uses before your", "item becomes unusable/breaks." }, new String[] { "all" });
	}

	/*
	 * initializes the custom durability mecanism on an item.
	 */
	@Override
	public void whenApplied(MMOItemBuilder item, StatData data) {
		double value = ((DoubleData) data).generateNewValue();
		item.addItemTag(new ItemTag("MMOITEMS_MAX_DURABILITY", value), new ItemTag("MMOITEMS_DURABILITY", value));
	}

	@Override
	public boolean canUse(RPGPlayer player, NBTItem item, boolean message) {
		if (item.hasTag("MMOITEMS_DURABILITY") && item.getDouble("MMOITEMS_DURABILITY") <= 0) {
			if (message) {
				Message.ZERO_DURABILITY.format(ChatColor.RED).send(player.getPlayer(), "cant-use-item");
				player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1.5f);
			}
			return false;
		}
		return true;
	}
}
