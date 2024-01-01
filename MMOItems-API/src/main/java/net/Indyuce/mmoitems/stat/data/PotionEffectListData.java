package net.Indyuce.mmoitems.stat.data;

import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PotionEffectListData implements StatData, Mergeable<PotionEffectListData> {
	private final List<PotionEffectData> effects = new ArrayList<>();

	// public PotionEffectListData(ConfigurationSection config) {
	// Validate.notNull(config, "Config must not be null");
	// for (String key : config.getKeys(false))
	// this.effects.add(new
	// PotionEffectData(config.getConfigurationSection(key)));
	// }

    public PotionEffectListData(PotionEffectData... effects) {
        add(effects);
    }

    public PotionEffectListData(Collection<PotionEffectData> effects) {
        add(effects);
    }

    public void add(PotionEffectData... effects) {
        for (PotionEffectData el : effects) this.effects.add(el);
    }

    public void add(Collection<PotionEffectData> effects) {
        this.effects.addAll(effects);
    }

    @NotNull
	public List<PotionEffectData> getEffects() {
		return effects;
	}

	@Override
	public boolean isEmpty() {
		return effects.isEmpty();
	}

	@Override
	public void mergeWith(PotionEffectListData data) {
		effects.addAll(data.effects);
	}

	@Override
	@NotNull
	public PotionEffectListData clone() { return new PotionEffectListData(effects); }

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof PotionEffectListData)) { return false; }

		if (((PotionEffectListData) obj).getEffects().size() != getEffects().size()) { return false; }

		for (PotionEffectData eff : ((PotionEffectListData) obj).getEffects()) {

			if (eff == null) { continue; }

			// COmpare
			boolean unmatched = true;
			for (PotionEffectData thi : getEffects()) {

				if (eff.equals(thi)) {
					unmatched = false;
					break; } }
			if (unmatched) { return false; }
		}
		return true;
	}
}
