package net.Indyuce.mmoitems.stat.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;

import net.Indyuce.mmoitems.api.CustomSound;
import net.Indyuce.mmoitems.api.itemgen.GeneratedItemBuilder;
import net.Indyuce.mmoitems.api.itemgen.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;

public class SoundListData implements StatData, Mergeable, RandomStatData {
	private final Map<CustomSound, SoundData> stats = new HashMap<>();

	public Set<CustomSound> getCustomSounds() {
		return stats.keySet();
	}

	public SoundData get(CustomSound sound) {
		return stats.get(sound);
	}

	public void set(CustomSound type, String sound, double volume, double pitch) {
		this.stats.put(type, new SoundData(sound, volume, pitch));
	}

	public int total() {
		return stats.size();
	}

	@Override
	public void merge(StatData data) {
		Validate.isTrue(data instanceof SoundListData, "Cannot merge two different stat data types");
		SoundListData cast = (SoundListData) data;
		cast.stats.keySet().forEach(key -> stats.put(key, cast.stats.get(key)));
	}

	@Override
	public StatData randomize(GeneratedItemBuilder builder) {
		return this;
	}
}