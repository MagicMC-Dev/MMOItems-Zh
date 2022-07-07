package net.Indyuce.mmoitems.stat.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.Validate;

import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import org.jetbrains.annotations.NotNull;

public class PotionEffectListData implements StatData, Mergeable<PotionEffectListData> {
	private final List<PotionEffectData> effects = new ArrayList<>();

	// public PotionEffectListData(ConfigurationSection config) {
	// Validate.notNull(config, "Config must not be null");
	// for (String key : config.getKeys(false))
	// this.effects.add(new
	// PotionEffectData(config.getConfigurationSection(key)));
	// }

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

	public PotionEffectListData(PotionEffectData... effects) {
		add(effects);
	}

	public void add(PotionEffectData... effects) {
		this.effects.addAll(Arrays.asList(effects));
	}

	public List<PotionEffectData> getEffects() {
		return effects;
	}

	@Override
	public boolean isEmpty() {
		return effects.isEmpty();
	}

	@Override
	public void merge(PotionEffectListData data) {
		effects.addAll(data.effects);
	}

	@Override
	public @NotNull PotionEffectListData cloneData() { return new PotionEffectListData(getEffects().toArray(new PotionEffectData[0])); }
}
