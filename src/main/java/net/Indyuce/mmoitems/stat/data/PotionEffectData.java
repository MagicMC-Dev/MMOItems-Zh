package net.Indyuce.mmoitems.stat.data;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.Indyuce.mmoitems.MMOUtils;

public class PotionEffectData {
	private final PotionEffect effect;

	public PotionEffectData(PotionEffectType type, int level) {
		effect = new PotionEffect(type, MMOUtils.getEffectDuration(type), level - 1, true, false);
	}

	public PotionEffectData(PotionEffectType type, double duration, int level) {
		effect = new PotionEffect(type, (int) (duration * 20), level - 1, true, false);
	}

	public double getDuration() {
		return (double) effect.getDuration() / 20;
	}

	public int getLevel() {
		return effect.getAmplifier() + 1;
	}

	public PotionEffectType getType() {
		return effect.getType();
	}

	public PotionEffect toEffect() {
		return effect;
	}
}
