package net.Indyuce.mmoitems.stat.data;

import net.Indyuce.mmoitems.util.MMOUtils;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Objects;

public class PotionEffectData {
	private final PotionEffectType type;
	private final double duration;
	private final int level;

	public PotionEffectData(PotionEffectType type, int level) {
		this(type, (double) MMOUtils.getEffectDuration(type) / 20d, level);
	}

	public PotionEffectData(PotionEffect effect) {
		this.type = effect.getType();
		this.duration = (double) effect.getDuration() / 20d;
		this.level = effect.getAmplifier() + 1;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PotionEffectData that = (PotionEffectData) o;
        return Double.compare(that.duration, duration) == 0 && level == that.level && type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, duration, level);
    }
}
