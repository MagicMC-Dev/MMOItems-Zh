package net.Indyuce.mmoitems.api.interaction;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.stat.data.SkullTextureData;
import net.Indyuce.mmoitems.stat.data.StringListData;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.version.VersionMaterial;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;

public class ItemSkin extends UseItem {
	public ItemSkin(Player player, NBTItem item) {
		super(player, item);
	}

	public ApplyResult applyOntoItem(NBTItem target, Type targetType) {
		if (targetType == Type.SKIN)
			return new ApplyResult(ResultType.NONE);

		if (MMOItems.plugin.getConfig().getBoolean("locked-skins") && target.getBoolean("MMOITEMS_HAS_SKIN")) {
			player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
			Message.SKIN_REJECTED.format(ChatColor.RED, "#item#", MMOUtils.getDisplayName(target.getItem()))
					.send(player);
			return new ApplyResult(ResultType.NONE);
		}

		boolean compatible = false;

		//SKIN//MMOItems.log("\u00a78SKIN \u00a7eCPT\u00a77 Applying onto " + MMOUtils.getDisplayName(target.getItem()));

		if (getMMOItem().hasData(ItemStats.COMPATIBLE_TYPES)) {
			//SKIN//MMOItems.log("\u00a78SKIN \u00a7eCPT\u00a77 Testing that TYPE is compatible: ");

			List<String> acceptedTypes = ((StringListData) getMMOItem().getData(ItemStats.COMPATIBLE_TYPES)).getList();

			for (String type : acceptedTypes) {
				//SKIN//MMOItems.log("\u00a78SKIN \u00a7eCPT\u00a7e >\u00a7f " + type);

				if (type.equalsIgnoreCase(targetType.getId())) {
					//SKIN//MMOItems.log("\u00a78SKIN \u00a7eCPT\u00a7a Matched");
					compatible = true; break; }
			}

			if (!compatible && acceptedTypes.size() > 0) {
				//SKIN//MMOItems.log("\u00a78SKIN \u00a7eCPT\u00a7c Incompatible");

				player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
				Message.SKIN_INCOMPATIBLE.format(ChatColor.RED, "#item#", MMOUtils.getDisplayName(target.getItem()))
						.send(player);
				return new ApplyResult(ResultType.NONE);
			}
		}

		if (getMMOItem().hasData(ItemStats.COMPATIBLE_IDS)) {
			//SKIN//MMOItems.log("\u00a78SKIN \u00a7eCPT\u00a77 Testing that ID is compatible: ");

			List<String> acceptedIDs = ((StringListData) getMMOItem().getData(ItemStats.COMPATIBLE_IDS)).getList();

			for (String id : acceptedIDs) {
				//SKIN//MMOItems.log("\u00a78SKIN \u00a7eCPT\u00a76 >\u00a7f " + id);

				if (id.equalsIgnoreCase(target.getString("MMOITEMS_ITEM_ID"))) {
					//SKIN//MMOItems.log("\u00a78SKIN \u00a7eCPT\u00a7a Matched");
					compatible = true;break; }
			}

			if (!compatible && acceptedIDs.size() > 0) {
				//SKIN//MMOItems.log("\u00a78SKIN \u00a7eCPT\u00a7c Incompatible");

				player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
				Message.SKIN_INCOMPATIBLE.format(ChatColor.RED, "#item#", MMOUtils.getDisplayName(target.getItem()))
						.send(player);
				return new ApplyResult(ResultType.NONE);
			}
		}

		// check for success rate
		double successRate = getNBTItem().getStat(ItemStats.SUCCESS_RATE.getId());
		if (successRate != 0)
			if (RANDOM.nextDouble() < 1 - successRate / 100) {
				player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
				Message.SKIN_BROKE.format(ChatColor.RED, "#item#", MMOUtils.getDisplayName(target.getItem()))
						.send(player);
				return new ApplyResult(ResultType.FAILURE);
			}

		// Apply skin
		ItemStack item = applySkin(target, getNBTItem(), getMMOItem());

		player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
		Message.SKIN_APPLIED.format(ChatColor.YELLOW, "#item#", MMOUtils.getDisplayName(target.getItem())).send(player);

		return new ApplyResult(item);
	}

	public static final String tagHasSkin = "MMOITEMS_HAS_SKIN";
	public static final String tagSkinID = "MMOITEMS_SKIN_ID";

	/**
	 * Applies the skin information from a skin consumable onto any item.
	 *
	 * @param target Target item that the skin has been <b>successfully</b> applied to
	 *
	 * @param skinItemNBT Skin consumable, as NBT
	 * @param skinItemMMO Skin consumable, as MMOItem
	 *
	 * @return Built ItemStack from the target NBT but with the skin data contained in the skin consumable
	 */
	@NotNull public static ItemStack applySkin(@NotNull NBTItem target, @NotNull NBTItem skinItemNBT, @NotNull MMOItem skinItemMMO) {

		target.addTag(new ItemTag(tagHasSkin, true));
		target.addTag(new ItemTag(tagSkinID, skinItemNBT.getString(tagSkinID)));
		if (skinItemNBT.getInteger("CustomModelData") != 0) {
			target.addTag(new ItemTag("CustomModelData", skinItemNBT.getInteger("CustomModelData"))); }
		if (!skinItemNBT.getString("MMOITEMS_ITEM_PARTICLES").isEmpty()) {
			target.addTag(new ItemTag("MMOITEMS_ITEM_PARTICLES", skinItemNBT.getString("MMOITEMS_ITEM_PARTICLES"))); }

		ItemStack item = target.toItem();
		if (item.getType() != skinItemNBT.getItem().getType()) { item.setType(skinItemNBT.getItem().getType()); }

		ItemMeta meta = item.getItemMeta();
		ItemMeta skinMeta = skinItemNBT.getItem().getItemMeta();
		if (skinMeta != null && meta != null) {

			if (skinMeta.isUnbreakable()) {
				meta.setUnbreakable(true);
				if (meta instanceof Damageable && skinMeta instanceof Damageable)
					((Damageable) meta).setDamage(((Damageable) skinMeta).getDamage());
			}

			if(skinMeta instanceof LeatherArmorMeta && meta instanceof LeatherArmorMeta)
				((LeatherArmorMeta) meta).setColor(((LeatherArmorMeta) skinMeta).getColor());

			if (skinItemMMO.hasData(ItemStats.SKULL_TEXTURE) && item.getType() == VersionMaterial.PLAYER_HEAD.toMaterial()
					&& skinItemNBT.getItem().getType() == VersionMaterial.PLAYER_HEAD.toMaterial()) {
				try {
					Field profileField = meta.getClass().getDeclaredField("profile");
					profileField.setAccessible(true);
					profileField.set(meta,
							((SkullTextureData) skinItemMMO.getData(ItemStats.SKULL_TEXTURE)).getGameProfile());
				} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
					MMOItems.plugin.getLogger().warning("Could not read skull texture");
				}
			}

			item.setItemMeta(meta);
		}

		return item;
	}

	/**
	 * Copies a skin from one item to another
	 *
	 * @param target Target item that you are copying the skin onto
	 *
	 * @param originalItemNBT Item with a skin already, as NBT. Operation will fail
	 *                        if it doesnt have a skin.
	 *
	 * @return Built ItemStack from the target NBT but with the skin data contained in the skin consumable
	 *
	 * @author Gunging
	 */
	@Nullable public static ItemStack applySkin(@NotNull NBTItem target, @NotNull NBTItem originalItemNBT) {

		// No skin no service
		if (!originalItemNBT.getBoolean("MMOITEMS_HAS_SKIN")) { return null; }

		// Copy over data
		target.addTag(new ItemTag("MMOITEMS_HAS_SKIN", true));
		target.addTag(new ItemTag("MMOITEMS_SKIN_ID", originalItemNBT.getString("MMOITEMS_ITEM_ID")));
		if (originalItemNBT.getInteger("CustomModelData") != 0) {
			target.addTag(new ItemTag("CustomModelData", originalItemNBT.getInteger("CustomModelData"))); }
		if (!originalItemNBT.getString("MMOITEMS_ITEM_PARTICLES").isEmpty()) {
			target.addTag(new ItemTag("MMOITEMS_ITEM_PARTICLES", originalItemNBT.getString("MMOITEMS_ITEM_PARTICLES"))); }

		// ItemMeta values copy-over
		ItemStack item = target.toItem();
		if (item.getType() != originalItemNBT.getItem().getType()) { item.setType(originalItemNBT.getItem().getType()); }

		ItemMeta meta = item.getItemMeta();
		ItemMeta originalMeta = originalItemNBT.getItem().getItemMeta();
		if (originalMeta != null && meta != null) {

			if (originalMeta.isUnbreakable()) {
				meta.setUnbreakable(true);
				if (meta instanceof Damageable && originalMeta instanceof Damageable)
					((Damageable) meta).setDamage(((Damageable) originalMeta).getDamage());
			}

			if(originalMeta instanceof LeatherArmorMeta && meta instanceof LeatherArmorMeta)
				((LeatherArmorMeta) meta).setColor(((LeatherArmorMeta) originalMeta).getColor());

			VolatileMMOItem originalVolatile = new VolatileMMOItem(originalItemNBT);
			if (originalVolatile.hasData(ItemStats.SKULL_TEXTURE) && item.getType() == VersionMaterial.PLAYER_HEAD.toMaterial()
					&& originalItemNBT.getItem().getType() == VersionMaterial.PLAYER_HEAD.toMaterial()) {

				try {
					Field profileField = meta.getClass().getDeclaredField("profile");
					profileField.setAccessible(true);
					profileField.set(meta,
							((SkullTextureData) originalVolatile.getData(ItemStats.SKULL_TEXTURE)).getGameProfile());
				} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
					MMOItems.plugin.getLogger().warning("Could not read skull texture");
				}
			}

			item.setItemMeta(meta);
		}

		return item;
	}

	public static class ApplyResult {
		private final ResultType type;
		private final ItemStack result;

		public ApplyResult(ResultType type) {
			this(null, type);
		}

		public ApplyResult(ItemStack result) {
			this(result, ResultType.SUCCESS);
		}

		public ApplyResult(ItemStack result, ResultType type) {
			this.type = type;
			this.result = result;
		}

		public ResultType getType() {
			return type;
		}

		public ItemStack getResult() {
			return result;
		}
	}

	public enum ResultType {
		FAILURE, NONE, SUCCESS
	}
}
