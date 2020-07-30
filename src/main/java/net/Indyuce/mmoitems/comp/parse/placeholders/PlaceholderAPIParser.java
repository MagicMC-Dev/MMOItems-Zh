package net.Indyuce.mmoitems.comp.parse.placeholders;

import org.bukkit.OfflinePlayer;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.asangarin.hexcolors.ColorParse;

public class PlaceholderAPIParser implements PlaceholderParser {
	public PlaceholderAPIParser() {
		PlaceholderExpansion expansion = new MMOItemsPlaceholders();
		expansion.getPlaceholderAPI().getLocalExpansionManager().register(expansion);
	}

	@Override
	public String parse(OfflinePlayer player, String string) {
		return new ColorParse('&', PlaceholderAPI.setPlaceholders(player, string.replace("%player%", player.getName())))
				.toChatColor();
	}
}
