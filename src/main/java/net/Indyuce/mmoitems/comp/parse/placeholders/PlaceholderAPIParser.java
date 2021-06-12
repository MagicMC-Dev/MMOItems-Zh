package net.Indyuce.mmoitems.comp.parse.placeholders;

import org.bukkit.OfflinePlayer;

import io.lumine.mythic.lib.MythicLib;
import me.clip.placeholderapi.PlaceholderAPI;

public class PlaceholderAPIParser implements PlaceholderParser {
	public PlaceholderAPIParser() {
		new MMOItemsPlaceholders().register();
	}

	@Override
	public String parse(OfflinePlayer player, String string) {
		return MythicLib.plugin.parseColors(PlaceholderAPI.setPlaceholders(player, string.replace("%player%", player.getName())));
	}
}
