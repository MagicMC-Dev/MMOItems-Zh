package net.Indyuce.mmoitems.stat.data;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.Indyuce.mmoitems.MMOUtils;

public class PotionEffectData {
	private final PotionEffectType type;
	private final double duration;
	private final int level;

	public PotionEffectData(PotionEffectType type, int level) {
		this(type, (double) MMOUtils.getEffectDuration(type) / 20d, level);
	}

	public PotionEffectData(PotionEffectType type, double duration, int level) {
		this.type = type;
		this.duration = duration;
		this.level = level;
	}

	public PotionEffectType getType() {
		return type;
	}

	public double getDuration() {
		return duration;
	}

	public int getLevel() {
		return level;
	}

	public PotionEffect toEffect() {
		return new PotionEffect(type, (int) (duration * 20), level - 1, true, false);
	}
}
