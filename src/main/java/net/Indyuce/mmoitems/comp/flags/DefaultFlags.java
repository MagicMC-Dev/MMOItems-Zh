package net.Indyuce.mmoitems.comp.flags;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class DefaultFlags implements FlagPlugin {
	@Override
	public boolean isPvpAllowed(Location loc) {
		return true;
	}

	@Override
	public boolean isFlagAllowed(Player player, CustomFlag flag) {
		return true;
	}

	@Override
	public boolean isFlagAllowed(Location loc, CustomFlag flag) {
		return true;
	}
}
