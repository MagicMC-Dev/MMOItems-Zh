package net.Indyuce.mmoitems.comp.placeholderapi;

import java.text.DecimalFormat;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.AltChar;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.NBTItem;

public class MMOItemsPlaceholders extends PlaceholderExpansion {
	private DecimalFormat oneDigit = new DecimalFormat("0.#"), twoDigits = new DecimalFormat("0.##");

	@Override
	public String getAuthor() {
		return "Indyuce";
	}

	@Override
	public String getIdentifier() {
		return "mmoitems";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	public String onPlaceholderRequest(Player player, String identifier) {
		if (identifier.startsWith("stat_")) {
			ItemStat stat = MMOItems.plugin.getStats().get(identifier.substring(5).toUpperCase());
			if (stat != null)
				return twoDigits.format(PlayerData.get(player).getStats().getStat(stat));
		}

		if (identifier.startsWith("ability_cd_"))
			PlayerData.get(player).getRemainingAbilityCooldown(identifier.substring(11));

		if (identifier.equals("durability"))
			return "" + (int) MMOLib.plugin.getNMS().getNBTItem(player.getInventory().getItemInMainHand()).getDouble("MMOITEMS_DURABILITY");

		if (identifier.equals("durability_max"))
			return "" + (int) MMOLib.plugin.getNMS().getNBTItem(player.getInventory().getItemInMainHand()).getDouble("MMOITEMS_MAX_DURABILITY");

		if (identifier.equals("durability_ratio")) {
			NBTItem item = MMOLib.plugin.getNMS().getNBTItem(player.getInventory().getItemInMainHand());
			double durability = item.getDouble("MMOITEMS_DURABILITY");
			double maxDurability = item.getDouble("MMOITEMS_MAX_DURABILITY");
			return oneDigit.format(durability / maxDurability * 100);
		}

		if (identifier.equals("durability_bar_square"))
			return getCurrentDurabilityBar(player.getInventory().getItemInMainHand(), AltChar.square, 10);

		if (identifier.equals("durability_bar_diamond"))
			return getCurrentDurabilityBar(player.getInventory().getItemInMainHand(), AltChar.diamond, 15);

		if (identifier.equals("durability_bar_thin"))
			return getCurrentDurabilityBar(player.getInventory().getItemInMainHand(), "|", 20);
		return null;
	}

	private String getCurrentDurabilityBar(ItemStack item, String barChar, int length) {
		NBTItem nbtItem = MMOLib.plugin.getNMS().getNBTItem(item);
		double durability = nbtItem.getDouble("MMOITEMS_DURABILITY");
		double maxDurability = nbtItem.getDouble("MMOITEMS_MAX_DURABILITY");
		long r = Math.round(durability / maxDurability * length);
		String bar = "" + ChatColor.GREEN;
		for (int j = 0; j < length; j++)
			bar += (j == r ? ChatColor.WHITE : "") + barChar;
		return bar;
	}
}
