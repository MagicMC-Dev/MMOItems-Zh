package net.Indyuce.mmoitems.comp.itemglow;

import org.apache.commons.lang.Validate;
import org.bukkit.Color;

public class TierColor {
	private final GlowColor glow;
	private final Color bukkit;

	public TierColor(String format, boolean glow) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		Validate.notNull(format, "String must not be null");

		bukkit = (Color) Color.class.getField(format.toUpperCase().replace("-", "_").replace(" ", "_")).get(Color.class);
		this.glow = glow ? new GlowColor(bukkit) : null;
	}

	public GlowColor toGlow() {
		return glow;
	}

	public org.bukkit.Color toBukkit() {
		return bukkit;
	}
}
