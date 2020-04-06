package net.Indyuce.mmoitems.stat.data;

import java.util.ArrayList;
import java.util.List;

public class EffectListData implements StatData, Mergeable {
	private final List<PotionEffectData> effects = new ArrayList<>();

	public EffectListData(PotionEffectData... effects) {
		add(effects);
	}

	public void add(PotionEffectData... effects) {
		for (PotionEffectData effect : effects)
			this.effects.add(effect);
	}

	public List<PotionEffectData> getEffects() {
		return effects;
	}

	@Override
	public void merge(Mergeable stat) {
		effects.addAll(((EffectListData) stat).effects);
	}
}
