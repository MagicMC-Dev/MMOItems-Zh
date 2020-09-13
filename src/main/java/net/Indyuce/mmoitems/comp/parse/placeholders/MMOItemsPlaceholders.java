package net.Indyuce.mmoitems.comp.parse.placeholders;

import java.text.DecimalFormat;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.NBTItem;
import net.mmogroup.mmolib.api.player.MMOPlayerData;
import net.mmogroup.mmolib.api.util.AltChar;
import net.mmogroup.mmolib.listener.DamageReduction;

public class MMOItemsPlaceholders extends PlaceholderExpansion {
	private final DecimalFormat oneDigit = new DecimalFormat("0.#"), twoDigits = new DecimalFormat("0.##");

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
		return MMOItems.plugin.getDescription().getVersion();
	}

	/**
	 * Because this is an internal class, you must override this method to let
	 * PlaceholderAPI know to not unregister your expansion class when
	 * PlaceholderAPI is reloaded
	 *
	 * @return true to persist through reloads
	 */
	@Override
	public boolean persist() {
		return true;
	}

	@Override
	public String onRequest(@Nullable OfflinePlayer player, @NotNull String identifier) {
		// registering before identifier.startsWith("stat_") to prevent issues
		// i don't register it in the starts with condition because it will mess
		// with substring
		if (identifier.equals("stat_defense_percent"))
			return twoDigits.format(100 - new DamageReduction.DefenseCalculator(MMOPlayerData.get(player)).getAppliedDamage(100)) + "%";
		if (identifier.startsWith("stat_")) {
			ItemStat stat = MMOItems.plugin.getStats().get(identifier.substring(5).toUpperCase());
			if (stat != null)
				return twoDigits.format(PlayerData.get(player).getStats().getStat(stat));
		}

		if (identifier.startsWith("ability_cd_")) {
			PlayerData data = PlayerData.get(player);
			return data.hasCooldownInfo(identifier.substring(11)) ? oneDigit.format(data.getCooldownInfo(identifier.substring(11)).getRemaining())
					: "0";
		}

		if (!player.isOnline())
			return null;

		if (identifier.equals("durability"))
			return "" + (int) MMOLib.plugin.getVersion().getWrapper().getNBTItem(player.getPlayer().getInventory().getItemInMainHand())
					.getDouble("MMOITEMS_DURABILITY");

		if (identifier.equals("durability_max"))
			return "" + (int) MMOLib.plugin.getVersion().getWrapper().getNBTItem(player.getPlayer().getInventory().getItemInMainHand())
					.getDouble("MMOITEMS_MAX_DURABILITY");

		if (identifier.equals("durability_ratio")) {
			NBTItem item = MMOLib.plugin.getVersion().getWrapper().getNBTItem(player.getPlayer().getInventory().getItemInMainHand());
			double durability = item.getDouble("MMOITEMS_DURABILITY");
			double maxDurability = item.getDouble("MMOITEMS_MAX_DURABILITY");
			return oneDigit.format(durability / maxDurability * 100);
		}

		if (identifier.equals("durability_bar_square"))
			return getCurrentDurabilityBar(player.getPlayer().getInventory().getItemInMainHand(), AltChar.square, 10);

		if (identifier.equals("durability_bar_diamond"))
			return getCurrentDurabilityBar(player.getPlayer().getInventory().getItemInMainHand(), AltChar.diamond, 15);

		if (identifier.equals("durability_bar_thin"))
			return getCurrentDurabilityBar(player.getPlayer().getInventory().getItemInMainHand(), "|", 20);
		return null;
	}

	private String getCurrentDurabilityBar(ItemStack item, String barChar, int length) {
		NBTItem nbtItem = MMOLib.plugin.getVersion().getWrapper().getNBTItem(item);
		double durability = nbtItem.getDouble("MMOITEMS_DURABILITY");
		double maxDurability = nbtItem.getDouble("MMOITEMS_MAX_DURABILITY");
		long r = Math.round(durability / maxDurability * length);
		String bar = "" + ChatColor.GREEN;
		for (int j = 0; j < length; j++)
			bar += (j == r ? ChatColor.WHITE : "") + barChar;
		return bar;
	}
}
