package net.Indyuce.mmoitems.stat.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;

import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;

public class PotionEffectListData implements StatData, Mergeable {
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
	public void merge(StatData data) {
		Validate.isTrue(data instanceof PotionEffectListData, "Cannot merge two different stat data types");
		effects.addAll(((PotionEffectListData) data).effects);
	}

	@Override
	public @NotNull StatData cloneData() { return new PotionEffectListData(getEffects().toArray(new PotionEffectData[0])); }

	@Override
	public boolean isClear() {
		return getEffects().size() == 0;
	}
}
