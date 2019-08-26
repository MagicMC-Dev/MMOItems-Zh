package net.Indyuce.mmoitems.comp.itemglow;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Color;
import org.inventivetalent.glow.GlowAPI;

public class GlowColor {
	private final GlowAPI.Color glow;

	private static final Map<Color, GlowAPI.Color> map = new HashMap<>();

	static {
		map.put(Color.AQUA, GlowAPI.Color.AQUA);
		map.put(Color.BLACK, GlowAPI.Color.BLACK);
		map.put(Color.BLUE, GlowAPI.Color.BLUE);
		map.put(Color.FUCHSIA, GlowAPI.Color.DARK_PURPLE);
		map.put(Color.GRAY, GlowAPI.Color.DARK_GRAY);
		map.put(Color.GREEN, GlowAPI.Color.DARK_GREEN);
		map.put(Color.LIME, GlowAPI.Color.GREEN);
		map.put(Color.NAVY, GlowAPI.Color.DARK_BLUE);
		map.put(Color.OLIVE, GlowAPI.Color.AQUA);
		map.put(Color.ORANGE, GlowAPI.Color.GOLD);
		map.put(Color.PURPLE, GlowAPI.Color.PURPLE);
		map.put(Color.RED, GlowAPI.Color.RED);
		map.put(Color.SILVER, GlowAPI.Color.GRAY);
		map.put(Color.WHITE, GlowAPI.Color.WHITE);
		map.put(Color.YELLOW, GlowAPI.Color.YELLOW);

		// no equivalent
		map.put(Color.TEAL, GlowAPI.Color.BLUE);
		map.put(Color.MAROON, GlowAPI.Color.DARK_RED);
	}

	public GlowColor(Color color) {
		glow = map.get(color);
	}

	public GlowAPI.Color get() {
		return glow;
	}
}
