package net.Indyuce.mmoitems.stat.data;

import net.Indyuce.mmoitems.api.CustomSound;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SoundListData implements StatData, Mergeable, RandomStatData<SoundListData> {
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

	/**
	 * @return Sound used, or null if none
	 */
	@Nullable
	public SoundData get(CustomSound sound) {
		return sounds.getOrDefault(sound, null);
	}

	public void set(CustomSound type, SoundData data) {
		this.sounds.put(type, data);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SoundListData)) { return false; }
		if (((SoundListData) obj).getCustomSounds().size() != getCustomSounds().size()) { return false; }

		for (CustomSound sound : ((SoundListData) obj).getCustomSounds()) {

			if (sound == null) { continue; }

			boolean unmatched = true;
			for (CustomSound thi : getCustomSounds()) {

				if (sound.equals(thi)) {
					unmatched = false;
					break;  } }
			if (unmatched) { return false; }
		}

		return true;
	}

	@Override
	public void merge(StatData data) {
		Validate.isTrue(data instanceof SoundListData, "Cannot merge two different stat data types");
		SoundListData cast = (SoundListData) data;
		cast.sounds.forEach(sounds::put);
	}

	@Override
	public @NotNull StatData cloneData() { return new SoundListData(mapData()); }

	@Override
	public boolean isEmpty() {
		return sounds.isEmpty();
	}

	@Override
	public SoundListData randomize(MMOItemBuilder builder) {
		return new SoundListData(new HashMap<>(sounds));
	}
}