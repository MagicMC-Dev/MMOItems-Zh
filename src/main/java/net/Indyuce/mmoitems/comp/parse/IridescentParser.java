package net.Indyuce.mmoitems.comp.parse;

import com.github.klyser8.iridescent.api.ColorUtil;
import org.bukkit.entity.Player;

public class IridescentParser implements StringInputParser {

	@Override
	public String parseInput(Player player, String input) {
		return ColorUtil.colorMessage(player, input, false);
	}
}
