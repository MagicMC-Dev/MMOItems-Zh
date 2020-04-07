package net.Indyuce.mmoitems.stat;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.StringStat;
import net.mmogroup.mmolib.api.item.NBTItem;
import net.mmogroup.mmolib.version.VersionMaterial;

public class SkullTextureStat extends StringStat {
	public SkullTextureStat() {
		super("SKULL_TEXTURE", VersionMaterial.PLAYER_HEAD.toItem(), "Skull Texture",
				new String[] { "The head texture &nvalue&7.", "Can be found on heads databases." }, new String[] { "all" },
				VersionMaterial.PLAYER_HEAD.toMaterial());
	}

	@Override
	public StatData whenInitialized(Object object) {
		Validate.isTrue(object instanceof ConfigurationSection, "Must specify a config section");
		ConfigurationSection config = (ConfigurationSection) object;

		String value = config.getString("value");
		Validate.notNull(value, "Could not load skull texture value");

		String format = config.getString("uuid");
		Validate.notNull(format, "Could not find skull texture UUID: re-enter your skull texture value and one will be selected randomly.");
		
		SkullTextureData skullTexture = new SkullTextureData(new GameProfile(UUID.fromString(format), null));
		skullTexture.getGameProfile().getProperties().put("textures", new Property("textures", value));

		return skullTexture;
	}

	@Override
	public void whenDisplayed(List<String> lore, FileConfiguration config, String id) {
	}

	@Override
	public boolean whenInput(EditionInventory inv, ConfigFile config, String message, Object... info) {
		config.getConfig().set(inv.getItemId() + ".skull-texture.value", message);
		config.getConfig().set(inv.getItemId() + ".skull-texture.uuid", UUID.randomUUID().toString());
		inv.registerItemEdition(config);
		inv.open();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + getName() + " successfully changed to " + message + ".");
		return true;
	}

	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		if (item.getItemStack().getType() != VersionMaterial.PLAYER_HEAD.toMaterial())
			return true;

		try {
			Field profileField = item.getMeta().getClass().getDeclaredField("profile");
			profileField.setAccessible(true);
			profileField.set(item.getMeta(), ((SkullTextureData) data).getGameProfile());
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			item.getMMOItem().log(Level.WARNING, "Could not read skull texture");
		}
		return true;
	}

	@Override
	public void whenLoaded(MMOItem mmoitem, NBTItem item) {
		try {
			Field profileField = item.getItem().getItemMeta().getClass().getDeclaredField("profile");
			profileField.setAccessible(true);
			mmoitem.setData(ItemStat.SKULL_TEXTURE, new SkullTextureData((GameProfile) profileField.get(item.getItem().getItemMeta())));
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
		}
	}

	public class SkullTextureData implements StatData {
		private final GameProfile profile;

		public SkullTextureData(GameProfile profile) {
			this.profile = profile;
		}

		public GameProfile getGameProfile() {
			return profile;
		}
	}
}
