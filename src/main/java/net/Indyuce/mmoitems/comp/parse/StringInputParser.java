package net.Indyuce.mmoitems.comp.parse;

import org.bukkit.entity.Player;

public interface StringInputParser {

	/*
	 * this interface is used to apply changes to string inputs when editing
	 * stats, for instance Iridescent applies weird ass color codes to strings
	 * and therefore all strings must be updated before being processed by stat
	 * edition methods
	 */
	String parseInput(Player player, String input);
}
