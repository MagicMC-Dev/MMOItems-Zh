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
	private final Map<CustomSound, SoundData> sounds;

	public SoundListData() {
		this(new HashMap<>());
	}

	public SoundListData(Map<CustomSound, SoundData> sounds) {
		this.sounds = sounds;
	}

	public Set<CustomSound> getCustomSounds() {
		return sounds.keySet();
	}

	public Map<CustomSound, SoundData> mapData() {
		return sounds;
	}

	public SoundData get(CustomSound sound) {
		return sounds.get(sound);
	}

	public void set(CustomSound type, String sound, double volume, double pitch) {
		this.sounds.put(type, new SoundData(sound, volume, pitch));
	}

	@Override
	public void merge(StatData data) {
		Validate.isTrue(data instanceof SoundListData, "Cannot merge two different stat data types");
		SoundListData cast = (SoundListData) data;
		cast.sounds.forEach((sound, soundData) -> sounds.put(sound, soundData));
	}

	@Override
	public StatData randomize(GeneratedItemBuilder builder) {
		return new SoundListData(new HashMap<>(sounds));
	}
}