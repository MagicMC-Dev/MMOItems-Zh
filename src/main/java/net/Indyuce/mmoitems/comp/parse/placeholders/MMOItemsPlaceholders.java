package net.Indyuce.mmoitems.comp.parse.placeholders;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.math.EvaluatedFormula;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.player.cooldown.CooldownInfo;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class MMOItemsPlaceholders extends PlaceholderExpansion {
	private final DecimalFormat oneDigit = new DecimalFormat("0.#"), twoDigits = new DecimalFormat("0.##");

	public MMOItemsPlaceholders() {
		oneDigit.setDecimalFormatSymbols(MythicLib.plugin.getMMOConfig().formatSymbols);
		twoDigits.setDecimalFormatSymbols(MythicLib.plugin.getMMOConfig().formatSymbols);
	}

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
			return twoDigits.format(100 - calculateDefense(MMOPlayerData.get(player))) + "%";

		if (identifier.startsWith("stat_elements") && player.isOnline()) {
			// index 0 = element
			// index 1 = defense/damage
			String[] param = identifier.split("_");

			if (param.length > 3) {
				String tag = "MMOITEMS_" + param[2].toUpperCase() + "_" + param[3].toUpperCase();

				double value = 0;
				for (EquipmentSlot slot : EquipmentSlot.values())
					if (hasItem((Player) player, slot)) {
						NBTItem nbtItem = NBTItem.get(((Player) player).getInventory().getItem(slot));
						if (nbtItem.hasTag(tag)) {
							value += nbtItem.getDouble(tag);
						}
					}

				return twoDigits.format(value);
			}
		}
		else if (identifier.startsWith("stat_")) {
			ItemStat stat = MMOItems.plugin.getStats().get(identifier.substring(5).toUpperCase());
			if (stat != null)
				return twoDigits.format(PlayerData.get(player).getStats().getStat(stat));
		}

		if (identifier.startsWith("ability_cd_")) {
			MMOPlayerData data = MMOPlayerData.get(player);
			CooldownInfo info = data.getCooldownMap().getInfo("mmoitems_skill_" + identifier.substring(11));
			return info == null ? "0" : oneDigit.format(info.getRemaining() / 1000d);
		}

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

	private double calculateDefense(MMOPlayerData data) {
		String formula = MythicLib.plugin.getConfig().getString("defense-application", "#damage# * (1 - (#defense# / (#defense# + 100)))");
		formula = formula.replace("#defense#", String.valueOf(data.getStatMap().getStat("DEFENSE")));
		formula = formula.replace("#damage#", String.valueOf(100));
		return Math.max(0, new EvaluatedFormula(formula).evaluate());
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
