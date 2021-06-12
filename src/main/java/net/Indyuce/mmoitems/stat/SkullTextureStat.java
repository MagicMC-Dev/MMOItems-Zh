package net.Indyuce.mmoitems.stat;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.SkullTextureData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.StringStat;

public class SkullTextureStat extends StringStat {
	public SkullTextureStat() {
		super("SKULL_TEXTURE", VersionMaterial.PLAYER_HEAD.toMaterial(), "Skull Texture",
				new String[] { "The head texture &nvalue&7.", "Can be found on heads databases." }, new String[] { "all" },
				VersionMaterial.PLAYER_HEAD.toMaterial());
	}

	@Override
	public RandomStatData whenInitialized(Object object) {
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
	public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
		inv.getEditedSection().set("skull-texture.value", message);
		inv.getEditedSection().set("skull-texture.uuid", UUID.randomUUID().toString());
		inv.registerTemplateEdition();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + getName() + " successfully changed to " + message + ".");
	}

	@Override
	public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StatData data) {
		if (item.getItemStack().getType() != VersionMaterial.PLAYER_HEAD.toMaterial())
			return;

		try {
			Field profileField = item.getMeta().getClass().getDeclaredField("profile");
			profileField.setAccessible(true);
			profileField.set(item.getMeta(), ((SkullTextureData) data).getGameProfile());
		} catch (NoSuchFieldException | IllegalAccessException exception) {
			throw new IllegalArgumentException(exception.getMessage());
		}
	}
	/**
	 * This stat is saved not as a custom tag, but as the vanilla HideFlag itself.
	 * Alas this is an empty array
	 */
	@NotNull
	@Override
	public ArrayList<ItemTag> getAppliedNBT(@NotNull StatData data) { return new ArrayList<>(); }

	@Override
	public void whenLoaded(@NotNull ReadMMOItem mmoitem) {
		try {
			Field profileField = mmoitem.getNBT().getItem().getItemMeta().getClass().getDeclaredField("profile");
			profileField.setAccessible(true);
			mmoitem.setData(ItemStats.SKULL_TEXTURE, new SkullTextureData((GameProfile) profileField.get(mmoitem.getNBT().getItem().getItemMeta())));
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException ignored) {}
	}

	/**
	 * This stat is saved not as a custom tag, but as the vanilla Head Texture itself.
	 * Alas this method returns null.
	 */
	@Nullable
	@Override
	public StatData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) { return null; }

	@NotNull
	@Override
	public StatData getClearStatData() { return new SkullTextureData(new GameProfile(UUID.fromString("df930b7b-a84d-4f76-90ac-33be6a5b6c88"), "gunging")); }
}
