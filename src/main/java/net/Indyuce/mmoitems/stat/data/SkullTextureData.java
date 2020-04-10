package net.Indyuce.mmoitems.stat.data;

import com.mojang.authlib.GameProfile;

import net.Indyuce.mmoitems.stat.data.type.StatData;

public class SkullTextureData implements StatData {
	private final GameProfile profile;

	public SkullTextureData(GameProfile profile) {
		this.profile = profile;
	}

	public GameProfile getGameProfile() {
		return profile;
	}
}