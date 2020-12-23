package net.Indyuce.mmoitems.stat;

import java.lang.reflect.Field;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import com.google.gson.JsonObject;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
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
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.item.NBTItem;
import net.mmogroup.mmolib.api.util.SmartGive;
import net.mmogroup.mmolib.version.VersionMaterial;

public class CanDeskin extends BooleanStat implements ConsumableItemInteraction {
	public CanDeskin() {
		super("CAN_DESKIN", Material.LEATHER, "Can Deskin?",
				new String[] { "Players can deskin their item", "and get their skin back", "from the item." }, new String[] { "consumable" });
	}

	// TODO needs some cleanup
	@Override
	public boolean handleConsumableEffect(InventoryClickEvent event, PlayerData playerData, Consumable consumable, NBTItem target, Type targetType) {
		String skinId = target.getString("MMOITEMS_SKIN_ID");
		Player player = playerData.getPlayer();

		if (consumable.getNBTItem().getBoolean("MMOITEMS_CAN_DESKIN") && !skinId.isEmpty()) {

			// Set target item to default skin
			String targetItemId = target.getString("MMOITEMS_ITEM_ID");
			target.removeTag("MMOITEMS_HAS_SKIN");
			target.removeTag("MMOITEMS_SKIN_ID");

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

			targetItem.setItemMeta(targetItemMeta);
			targetItem.setType(originalItem.getType());
			target.getItem().setAmount(0);
			new SmartGive(player).give(targetItem);

			// Give back skin item
			MMOItemTemplate template = MMOItems.plugin.getTemplates().getTemplateOrThrow(Type.SKIN, skinId);
			MMOItem mmoitem = template.newBuilder(playerData.getRPG()).build();
			ItemStack item = mmoitem.newBuilder().build();

			new SmartGive(player).give(item);
			Message.SKIN_REMOVED.format(ChatColor.YELLOW, "#item#", MMOUtils.getDisplayName(targetItem)).send(player);
			return true;
		}
		return false;
	}
}
