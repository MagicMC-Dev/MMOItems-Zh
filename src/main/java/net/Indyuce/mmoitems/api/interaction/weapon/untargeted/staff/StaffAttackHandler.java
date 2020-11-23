package net.Indyuce.mmoitems.api.interaction.weapon.untargeted.staff;

import java.util.Random;

import org.bukkit.Location;

import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.mmogroup.mmolib.api.item.NBTItem;

public interface StaffAttackHandler {
	void handle(CachedStats stats, NBTItem nbt, double attackDamage, double range);

	Random random = new Random();

	default Location getGround(Location loc) {
		for (int j = 0; j < 20; j++) {
			if (loc.getBlock().getType().isSolid())
				return loc;
			loc.add(0, -1, 0);
		}
		return loc;
	}
}
