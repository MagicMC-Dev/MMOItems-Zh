package net.Indyuce.mmoitems.stat.data;

import com.mojang.authlib.GameProfile;

import net.Indyuce.mmoitems.api.item.template.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;

public class SkullTextureData implements StatData, RandomStatData {
	private final GameProfile profile;

	public SkullTextureData(GameProfile profile) {
		this.profile = profile;
	}

	public GameProfile getGameProfile() {
		return profile;
	}

	@Override
	public StatData randomize(MMOItemBuilder builder) {
		return this;
	}
}