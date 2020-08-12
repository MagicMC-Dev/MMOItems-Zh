package net.Indyuce.mmoitems.api.item.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.Indyuce.mmoitems.MMOItems;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.version.VersionMaterial;

public class CustomSkull extends ConfigItem {
	private String textureValue;

	public CustomSkull(String id, String textureValue) {
		this(id, textureValue, null);
	}

	public CustomSkull(String id, String textureValue, String name, String... lore) {
		super(id, VersionMaterial.PLAYER_HEAD.toMaterial(), name, lore);

		this.textureValue = textureValue;
	}

	public void updateItem() {
		setItem(VersionMaterial.PLAYER_HEAD.toItem());
		ItemMeta meta = getItem().getItemMeta();
		meta.setDisplayName(MMOLib.plugin.parseColors(getName()));
		meta.addItemFlags(ItemFlag.values());

		GameProfile gameProfile = new GameProfile(UUID.randomUUID(), null);
		gameProfile.getProperties().put("textures", new Property("textures", textureValue));
		try {
			Field profileField = meta.getClass().getDeclaredField("profile");
			profileField.setAccessible(true);
			profileField.set(meta, gameProfile);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException exception) {
			MMOItems.plugin.getLogger().log(Level.WARNING, "Could not load skull texture");
		}

		if (hasLore()) {
			List<String> lore = new ArrayList<>();
			getLore().forEach(str -> lore.add(ChatColor.GRAY + MMOLib.plugin.parseColors(str)));
			meta.setLore(lore);
		}

		getItem().setItemMeta(meta);
	}
}
