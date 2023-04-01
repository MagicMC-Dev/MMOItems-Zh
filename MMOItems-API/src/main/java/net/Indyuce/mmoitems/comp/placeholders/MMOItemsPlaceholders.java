package net.Indyuce.mmoitems.comp.placeholders;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.manager.StatManager;
import io.lumine.mythic.lib.util.DefenseFormula;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MMOItemsPlaceholders extends PlaceholderExpansion {

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
		if (identifier.equals("stat_defense_percent")) {
			final double defenseStat = MMOPlayerData.get(player).getStatMap().getStat("DEFENSE");
			final double damageReduction = 100 - new DefenseFormula().getAppliedDamage(defenseStat, 100);
			return MythicLib.plugin.getMMOConfig().decimal.format(damageReduction);
		}

		if (identifier.startsWith("stat_")) {
			final String stat = UtilityMethods.enumName(identifier.substring(5));
			return StatManager.format(stat, MMOPlayerData.get(player));
		}

		if (identifier.startsWith("ability_cd_"))
			return MythicLib.plugin.getMMOConfig().decimal.format(MMOPlayerData.get(player).getCooldownMap().getCooldown("skill_" + identifier.substring(11)));

		if(identifier.startsWith("type_")) {
			String t = identifier.substring(5, identifier.lastIndexOf("_")).toUpperCase();
			if(!MMOItems.plugin.getTypes().has(t)) return "Invalid type";
			Type type = Type.get(t);
			String pholder = identifier.substring(6 + t.length()).toLowerCase();
			if ("total".equals(pholder))
				return "" + MMOItems.plugin.getTemplates().getTemplates(type).size();
			return type.getName();
			/*switch(pholder) {
				case "total":
					return "" + MMOItems.plugin.getTemplates().getTemplates(type).size();
				default:
					return type.getName();
			}*/
		}

		if(identifier.startsWith("tier_")) {
			String t = identifier.substring(5).toUpperCase();
			if(!MMOItems.plugin.getTiers().has(t)) return "Invalid tier";
			return MMOItems.plugin.getTiers().get(t).getName();
		}

		if (!player.isOnline())
			return null;

		if (identifier.equals("durability")) {
			NBTItem nbt = MythicLib.plugin.getVersion().getWrapper().getNBTItem(player.getPlayer().getInventory().getItemInMainHand());
			return String.valueOf(nbt.hasTag("MMOITEMS_DURABILITY") ? nbt.getInteger("MMOITEMS_DURABILITY") : nbt.getInteger("MMOITEMS_MAX_DURABILITY"));
		}

		if (identifier.equals("durability_max"))
			return "" + (int) MythicLib.plugin.getVersion().getWrapper().getNBTItem(player.getPlayer().getInventory().getItemInMainHand())
					.getDouble("MMOITEMS_MAX_DURABILITY");

		if (identifier.equals("durability_ratio")) {
			NBTItem item = MythicLib.plugin.getVersion().getWrapper().getNBTItem(player.getPlayer().getInventory().getItemInMainHand());
			double durability = item.getDouble("MMOITEMS_DURABILITY");
			double maxDurability = item.getDouble("MMOITEMS_MAX_DURABILITY");
			return MythicLib.plugin.getMMOConfig().decimal.format(durability / maxDurability * 100);
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
		NBTItem nbtItem = MythicLib.plugin.getVersion().getWrapper().getNBTItem(item);
		double durability = nbtItem.getDouble("MMOITEMS_DURABILITY");
		double maxDurability = nbtItem.getDouble("MMOITEMS_MAX_DURABILITY");
		long r = Math.round(durability / maxDurability * length);
		StringBuilder bar = new StringBuilder("" + ChatColor.GREEN);
		for (int j = 0; j < length; j++)
			bar.append(j == r ? ChatColor.WHITE : "").append(barChar);
		return bar.toString();
	}

	private boolean hasItem(Player player, EquipmentSlot slot) {
		return player.getInventory().getItem(slot) != null && player.getInventory().getItem(slot).getType() != Material.AIR;
	}
}
