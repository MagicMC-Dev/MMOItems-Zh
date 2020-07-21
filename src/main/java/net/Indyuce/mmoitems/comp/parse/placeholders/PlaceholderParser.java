package net.Indyuce.mmoitems.comp.parse.placeholders;

import org.bukkit.OfflinePlayer;

public interface PlaceholderParser {
	public String parse(OfflinePlayer player, String string);
}
