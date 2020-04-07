package net.Indyuce.mmoitems.stat.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;

import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;

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
	public void merge(StatData data) {
		Validate.isTrue(data instanceof EffectListData, "Cannot merge two different stat data types");
		effects.addAll(((EffectListData) data).effects);
	}
}
