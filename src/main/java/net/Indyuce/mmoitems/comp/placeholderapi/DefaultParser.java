package net.Indyuce.mmoitems.comp.placeholderapi;

import org.bukkit.OfflinePlayer;

public class DefaultParser implements PlaceholderParser {
	@Override
	public String parse(OfflinePlayer player, String string) {
		return string.replace("%player%", player.getName());
	}
}
