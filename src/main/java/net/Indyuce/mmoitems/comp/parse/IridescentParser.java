package net.Indyuce.mmoitems.comp.parse;

import org.bukkit.entity.Player;

import com.github.klyser8.iridescent.util.ColorUtil;

public class IridescentParser implements StringInputParser {

	@Override
	public String parseInput(Player player, String input) {
		return ColorUtil.colorMessage(player, input, false);
	}
}
