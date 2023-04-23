package net.Indyuce.mmoitems.stat;

import com.google.gson.JsonObject;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.util.SmartGive;
import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.interaction.ItemSkin;
import net.Indyuce.mmoitems.util.MMOUtils;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.interaction.Consumable;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.stat.data.ParticleData;
import net.Indyuce.mmoitems.stat.data.SkullTextureData;
import net.Indyuce.mmoitems.stat.type.BooleanStat;
import net.Indyuce.mmoitems.stat.type.ConsumableItemInteraction;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public class CanDeskin extends BooleanStat implements ConsumableItemInteraction {
	public CanDeskin() {
		super("CAN_DESKIN", Material.LEATHER, "Can Deskin?",
				new String[] { "Players can deskin their item", "and get their skin back", "from the item." }, new String[] { "consumable" });
	}

	@Override
	public boolean handleConsumableEffect(@NotNull InventoryClickEvent event, @NotNull PlayerData playerData, @NotNull Consumable consumable, @NotNull NBTItem target, Type targetType) {
		final String skinId = target.getString(ItemSkin.SKIN_ID_TAG);
		Player player = playerData.getPlayer();

		if (consumable.getNBTItem().getBoolean("MMOITEMS_CAN_DESKIN") && !skinId.isEmpty()) {

			// Set target item to default skin
			String targetItemId = target.getString("MMOITEMS_ITEM_ID");
			target.removeTag(ItemSkin.SKIN_ID_TAG);

			MMOItemTemplate targetTemplate = MMOItems.plugin.getTemplates().getTemplateOrThrow(targetType, targetItemId);
			MMOItem originalMmoitem = targetTemplate.newBuilder(playerData.getRPG()).build();
			ItemStack originalItem = targetTemplate.newBuilder(playerData.getRPG()).build().newBuilder().build();

			int originalCustomModelData = originalItem.getItemMeta().hasCustomModelData() ? originalItem.getItemMeta().getCustomModelData() : -1;
			if (originalCustomModelData != -1)
				target.addTag(new ItemTag("CustomModelData", originalCustomModelData));
			else
				target.removeTag("CustomModelData");

			if (originalMmoitem.hasData(ItemStats.ITEM_PARTICLES)) {
				JsonObject itemParticles = ((ParticleData) originalMmoitem.getData(ItemStats.ITEM_PARTICLES)).toJson();
				target.addTag(new ItemTag("MMOITEMS_ITEM_PARTICLES", itemParticles.toString()));
			} else
				target.removeTag("MMOITEMS_ITEM_PARTICLES");

			ItemStack targetItem = target.toItem();
			ItemMeta targetItemMeta = targetItem.getItemMeta();
			ItemMeta originalItemMeta = originalItem.getItemMeta();

			if (targetItemMeta.isUnbreakable()) {
				targetItemMeta.setUnbreakable(originalItemMeta.isUnbreakable());
				if (targetItemMeta instanceof Damageable && originalItemMeta instanceof Damageable)
					((Damageable) targetItemMeta).setDamage(((Damageable) originalItemMeta).getDamage());
			}

			if (targetItemMeta instanceof LeatherArmorMeta && originalItemMeta instanceof LeatherArmorMeta)
				((LeatherArmorMeta) targetItemMeta).setColor(((LeatherArmorMeta) originalItemMeta).getColor());

			if (target.hasTag("SkullOwner") && (targetItem.getType() == VersionMaterial.PLAYER_HEAD.toMaterial())
					&& (originalItem.getType() == VersionMaterial.PLAYER_HEAD.toMaterial())) {
				try {
					Field profileField = targetItemMeta.getClass().getDeclaredField("profile");
					profileField.setAccessible(true);
					profileField.set(targetItemMeta, ((SkullTextureData) originalMmoitem.getData(ItemStats.SKULL_TEXTURE)).getGameProfile());
				} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
					MMOItems.plugin.getLogger().warning("Could not read skull texture");
				}
			}

			// Update deskined item
			final ItemStack updated = target.getItem();
			updated.setItemMeta(targetItemMeta);
			updated.setType(originalItem.getType());

			// Give back skin item
			MMOItemTemplate template = MMOItems.plugin.getTemplates().getTemplateOrThrow(Type.SKIN, skinId);
			MMOItem mmoitem = template.newBuilder(playerData.getRPG()).build();
			new SmartGive(player).give(mmoitem.newBuilder().build());

			Message.SKIN_REMOVED.format(ChatColor.YELLOW, "#item#", MMOUtils.getDisplayName(targetItem)).send(player);
			return true;
		}
		return false;
	}
}
