package net.Indyuce.mmoitems.api.interaction.weapon.untargeted.lute;

import java.util.Random;

import org.bukkit.Sound;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.mmogroup.mmolib.api.item.NBTItem;

public interface LuteAttackHandler {
	public void handle(CachedStats stats, NBTItem nbt, double attackDamage, double range, Vector weight, Sound sound);

	static final Random random = new Random();
}
