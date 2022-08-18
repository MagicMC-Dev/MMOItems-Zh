package net.Indyuce.mmoitems.comp.parse.placeholders;

import org.bukkit.OfflinePlayer;

@Deprecated
public class DefaultPlaceholderParser implements PlaceholderParser {
	
	@Override
	public String parse(OfflinePlayer player, String string) {
		return string.replace("%player%", player.getName());
	}
}
