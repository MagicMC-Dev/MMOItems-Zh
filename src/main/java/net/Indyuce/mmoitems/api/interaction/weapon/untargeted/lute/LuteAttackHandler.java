package net.Indyuce.mmoitems.api.interaction.weapon.untargeted.lute;

import java.util.Random;

import org.bukkit.Sound;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;

public interface LuteAttackHandler {
	public void handle(TemporaryStats stats, NBTItem nbt, double attackDamage, double range, Vector weight, Sound sound);

	static final Random random = new Random();
}
