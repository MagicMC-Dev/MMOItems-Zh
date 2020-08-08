package net.Indyuce.mmoitems.comp.parse.placeholders;

import org.bukkit.OfflinePlayer;

import me.clip.placeholderapi.PlaceholderAPI;
import net.asangarin.hexcolors.ColorParse;

public class PlaceholderAPIParser implements PlaceholderParser {
	public PlaceholderAPIParser() {
		new MMOItemsPlaceholders().register();
	}

	@Override
	public String parse(OfflinePlayer player, String string) {
		return new ColorParse('&', PlaceholderAPI.setPlaceholders(player, string.replace("%player%", player.getName())))
				.toChatColor();
	}
}
