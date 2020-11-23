package net.Indyuce.mmoitems.api.interaction;

import com.google.gson.JsonObject;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.event.item.ApplySoulboundEvent;
import net.Indyuce.mmoitems.api.event.item.BreakSoulboundEvent;
import net.Indyuce.mmoitems.api.event.item.DeconstructItemEvent;
import net.Indyuce.mmoitems.api.event.item.IdentifyItemEvent;
import net.Indyuce.mmoitems.api.event.item.RepairItemEvent;
import net.Indyuce.mmoitems.api.event.item.UpgradeItemEvent;
import net.Indyuce.mmoitems.api.interaction.util.DurabilityItem;
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.item.util.DynamicLore;
import net.Indyuce.mmoitems.api.item.util.identify.IdentifiedItem;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.comp.flags.FlagPlugin.CustomFlag;
import net.Indyuce.mmoitems.stat.Soulbound;
import net.Indyuce.mmoitems.stat.data.ParticleData;
import net.Indyuce.mmoitems.stat.data.PotionEffectListData;
import net.Indyuce.mmoitems.stat.data.SkullTextureData;
import net.Indyuce.mmoitems.stat.data.SoulboundData;
import net.Indyuce.mmoitems.stat.data.UpgradeData;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.item.NBTItem;
import net.mmogroup.mmolib.api.util.SmartGive;
import net.mmogroup.mmolib.version.VersionMaterial;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.lang.reflect.Field;
import java.util.List;

public class Consumable extends UseItem {
	public Consumable(Player player, NBTItem item) {
		super(player, item);
	}

	@Override
	public boolean canBeUsed() {
		return MMOItems.plugin.getFlags().isFlagAllowed(player, CustomFlag.MI_CONSUMABLES) && playerData.getRPG().canUse(getNBTItem(), true);
	}

	/**
	 * @param event
	 *            The click event
	 * @param target
	 *            The item on which the consumable is being applied
	 * @return If the consumable has applied at least once of its item options
	 *         ie if it should be consumed
	 */
	public boolean useOnItem(InventoryClickEvent event, NBTItem target) {
		if (event.getClickedInventory() != event.getWhoClicked().getInventory())
			return false;

		/*
		 * Unidentified items do not have any type, so you must check if the
		 * item has a type first.
		 */
		Type targetType = target.getType();
		if (targetType == null) {
			if (getNBTItem().getBoolean("MMOITEMS_CAN_IDENTIFY") && target.hasTag("MMOITEMS_UNIDENTIFIED_ITEM")) {

				IdentifyItemEvent called = new IdentifyItemEvent(playerData, mmoitem, target);
				Bukkit.getPluginManager().callEvent(called);
				if (called.isCancelled())
					return false;

				event.setCurrentItem(new IdentifiedItem(target).identify());
				Message.SUCCESSFULLY_IDENTIFIED.format(ChatColor.YELLOW, "#item#", MMOUtils.getDisplayName(event.getCurrentItem())).send(player);
				player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
				return true;
			}
			return false;
		}

		/*
		 * Deconstructing an item, usually consumables do not deconstruct and
		 * repair items at the same time so there should not be any problem with
		 * that
		 */
		String itemTierTag = target.getString("MMOITEMS_TIER");
		if (!itemTierTag.equals("") && getNBTItem().getBoolean("MMOITEMS_CAN_DECONSTRUCT")) {
			ItemTier tier = MMOItems.plugin.getTiers().get(itemTierTag);
			List<ItemStack> loot = tier.getDeconstructedLoot(playerData);
			if (!loot.isEmpty()) {

				DeconstructItemEvent called = new DeconstructItemEvent(playerData, mmoitem, target, loot);
				Bukkit.getPluginManager().callEvent(called);
				if (called.isCancelled())
					return false;

				Message.SUCCESSFULLY_DECONSTRUCTED.format(ChatColor.YELLOW, "#item#", MMOUtils.getDisplayName(event.getCurrentItem())).send(player);
				event.getCurrentItem().setAmount(event.getCurrentItem().getAmount() - 1);
				for (ItemStack drop : player.getInventory().addItem(loot.toArray(new ItemStack[0])).values())
					player.getWorld().dropItem(player.getLocation(), drop);
				player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
				return true;
			}
		}

		/*
		 * Upgrading an item, it is sbetter not to repair an item while
		 * upgrading it.
		 */
		if (mmoitem.hasData(ItemStats.UPGRADE) && target.hasTag("MMOITEMS_UPGRADE")) {
			if (target.getItem().getAmount() > 1) {
				Message.CANT_UPGRADED_STACK.format(ChatColor.RED).send(player);
				player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
				return false;
			}

			MMOItem targetMMO = new LiveMMOItem(target);
			UpgradeData targetSharpening = (UpgradeData) targetMMO.getData(ItemStats.UPGRADE);
			if (!targetSharpening.canLevelUp()) {
				Message.MAX_UPGRADES_HIT.format(ChatColor.RED).send(player);
				player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
				return false;
			}

			UpgradeData consumableSharpening = (UpgradeData) mmoitem.getData(ItemStats.UPGRADE);
			if (!consumableSharpening.matchesReference(targetSharpening)) {
				Message.WRONG_UPGRADE_REFERENCE.format(ChatColor.RED).send(player);
				player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
				return false;
			}

			UpgradeItemEvent called = new UpgradeItemEvent(playerData, mmoitem, targetMMO, consumableSharpening, targetSharpening);
			Bukkit.getPluginManager().callEvent(called);
			if (called.isCancelled())
				return false;

			targetSharpening.upgrade(targetMMO);
			NBTItem result = targetMMO.newBuilder().buildNBT();

			/*
			 * Safe check, if the specs the item has after ugprade are too high
			 * for the player, then cancel upgrading because the player would
			 * not be able to use it.
			 */
			if (MMOItems.plugin.getLanguage().upgradeRequirementsCheck && !playerData.getRPG().canUse(result, false)) {
				Message.UPGRADE_REQUIREMENT_SAFE_CHECK.format(ChatColor.RED).send(player);
				player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
				return false;
			}

			if (random.nextDouble() > consumableSharpening.getSuccess() * targetSharpening.getSuccess()) {
				Message.UPGRADE_FAIL.format(ChatColor.RED).send(player);
				if (targetSharpening.destroysOnFail())
					event.getCurrentItem().setAmount(0);
				player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 2);
				return true;
			}

			Message.UPGRADE_SUCCESS.format(ChatColor.YELLOW, "#item#", MMOUtils.getDisplayName(event.getCurrentItem())).send(player);
			event.getCurrentItem().setItemMeta(new DynamicLore(result).build().getItemMeta());
			player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
			return true;
		}

		/*
		 * applying a soulbound onto an item. it does not work if the item
		 * already has a soulbound, and it has a chance to successfully apply.
		 */
		double soulbindingChance = getNBTItem().getStat(ItemStats.SOULBINDING_CHANCE);
		if (soulbindingChance > 0) {
			if (target.getItem().getAmount() > 1) {
				Message.CANT_BIND_STACKED.format(ChatColor.RED).send(player, "soulbound");
				return false;
			}

			MMOItem targetMMO = new VolatileMMOItem(target);
			if (targetMMO.hasData(ItemStats.SOULBOUND)) {
				SoulboundData data = (SoulboundData) targetMMO.getData(ItemStats.SOULBOUND);
				Message.CANT_BIND_ITEM.format(ChatColor.RED, "#player#", data.getName(), "#level#", MMOUtils.intToRoman(data.getLevel())).send(player,
						"soulbound");
				return false;
			}

			if (random.nextDouble() < soulbindingChance / 100) {

				ApplySoulboundEvent called = new ApplySoulboundEvent(playerData, mmoitem, target);
				Bukkit.getPluginManager().callEvent(called);
				if (called.isCancelled())
					return false;

				int soulboundLevel = (int) Math.max(1, getNBTItem().getStat(ItemStats.SOULBOUND_LEVEL));
				(targetMMO = new LiveMMOItem(target)).setData(ItemStats.SOULBOUND,
						((Soulbound) ItemStats.SOULBOUND).newSoulboundData(player.getUniqueId(), player.getName(), soulboundLevel));
				target.getItem().setItemMeta(targetMMO.newBuilder().build().getItemMeta());
				Message.SUCCESSFULLY_BIND_ITEM
						.format(ChatColor.YELLOW, "#item#", MMOUtils.getDisplayName(target.getItem()), "#level#", MMOUtils.intToRoman(soulboundLevel))
						.send(player, "soulbound");
				player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
				return true;
			}

			Message.UNSUCCESSFUL_SOULBOUND.format(ChatColor.RED).send(player, "soulbound");
			player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
			return true;
		}

		/*
		 * Breaking the item's current soulbound. It has a random factor
		 * determined by the soulbound break chance, and the consumable needs to
		 * have at least the soulbound's level to be able to break the item
		 * soulbound.
		 */
		double soulboundBreakChance = getNBTItem().getStat(ItemStats.SOULBOUND_BREAK_CHANCE);
		if (soulboundBreakChance > 0) {
			MMOItem targetMMO = new VolatileMMOItem(target);
			if (!targetMMO.hasData(ItemStats.SOULBOUND)) {
				Message.NO_SOULBOUND.format(ChatColor.RED).send(player, "soulbound");
				player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
				return false;
			}

			SoulboundData soulbound = (SoulboundData) targetMMO.getData(ItemStats.SOULBOUND);

			// check for soulbound level
			if (Math.max(1, getNBTItem().getStat(ItemStats.SOULBOUND_LEVEL)) < soulbound.getLevel()) {
				Message.LOW_SOULBOUND_LEVEL.format(ChatColor.RED, "#level#", MMOUtils.intToRoman(soulbound.getLevel())).send(player, "soulbound");
				return false;
			}

			if (random.nextDouble() < soulboundBreakChance / 100) {

				BreakSoulboundEvent called = new BreakSoulboundEvent(playerData, mmoitem, target);
				Bukkit.getPluginManager().callEvent(called);
				if (called.isCancelled())
					return false;

				(targetMMO = new LiveMMOItem(target)).removeData(ItemStats.SOULBOUND);
				target.getItem().setItemMeta(targetMMO.newBuilder().build().getItemMeta());
				Message.SUCCESSFULLY_BREAK_BIND.format(ChatColor.YELLOW, "#level#", MMOUtils.intToRoman(soulbound.getLevel())).send(player,
						"soulbound");
				player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 2);
			} else {
				Message.UNSUCCESSFUL_SOULBOUND_BREAK.format(ChatColor.RED).send(player, "soulbound");
				player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 0);
			}

			return true;
		}

		/*
		 * Item repairing, does not apply if there's no repair power or if the
		 * item still has all its uses left
		 */
		int repairPower = (int) getNBTItem().getStat(ItemStats.REPAIR);
		if (repairPower > 0) {

			// custom durability
			if (target.hasTag("MMOITEMS_DURABILITY")) {

				RepairItemEvent called = new RepairItemEvent(playerData, mmoitem, target, repairPower);
				Bukkit.getPluginManager().callEvent(called);
				if (called.isCancelled())
					return false;

				DurabilityItem durItem = new DurabilityItem(player, target);
				if (durItem.getDurability() < durItem.getMaxDurability()) {
					target.getItem().setItemMeta(durItem.addDurability(called.getRepaired()).toItem().getItemMeta());
					Message.REPAIRED_ITEM
							.format(ChatColor.YELLOW, "#item#", MMOUtils.getDisplayName(target.getItem()), "#amount#", "" + called.getRepaired())
							.send(player);
				}
				return true;
			}

			// vanilla durability
			if (!target.getBoolean("Unbreakable") && target.getItem().hasItemMeta() && target.getItem().getItemMeta() instanceof Damageable
					&& ((Damageable) target.getItem().getItemMeta()).getDamage() > 0) {

				RepairItemEvent called = new RepairItemEvent(playerData, mmoitem, target, repairPower);
				Bukkit.getPluginManager().callEvent(called);
				if (called.isCancelled())
					return false;

				ItemMeta meta = target.getItem().getItemMeta();
				((Damageable) meta).setDamage(Math.max(0, ((Damageable) meta).getDamage() - called.getRepaired()));
				target.getItem().setItemMeta(meta);
				Message.REPAIRED_ITEM
						.format(ChatColor.YELLOW, "#item#", MMOUtils.getDisplayName(target.getItem()), "#amount#", "" + called.getRepaired())
						.send(player);
				return true;
			}
		}

		/*
		 * Item Deskinning
		 * Sets the target item into its original Custom Model Data
		 * and returning skin item to the player
		 */
		String skinId = target.getString("MMOITEMS_SKIN_ID");
		if(getNBTItem().getBoolean("MMOITEMS_CAN_DESKIN") && !skinId.isEmpty()) {
			Player player = (Player) event.getWhoClicked();

			//Set target item to default skin
			String targetItemId = target.getString("MMOITEMS_ITEM_ID");
			target.removeTag("MMOITEMS_HAS_SKIN");
			target.removeTag("MMOITEMS_SKIN_ID");

			MMOItemTemplate targetTemplate = MMOItems.plugin.getTemplates().getTemplateOrThrow(targetType, targetItemId);
			MMOItem originalMmoitem = targetTemplate.newBuilder(playerData.getRPG()).build();
			ItemStack originalItem = targetTemplate.newBuilder(playerData.getRPG()).build().newBuilder().build();

			int originalCustomModelData = originalItem.getItemMeta().hasCustomModelData() ? originalItem.getItemMeta().getCustomModelData() : -1;
			if(originalCustomModelData != -1) {
				target.addTag(new ItemTag("CustomModelData", originalCustomModelData));
			} else {
				target.removeTag("CustomModelData");
			}

			if(originalMmoitem.hasData(ItemStats.ITEM_PARTICLES)) {
				JsonObject itemParticles = ((ParticleData) originalMmoitem.getData(ItemStats.ITEM_PARTICLES)).toJson();
				target.addTag(new ItemTag("MMOITEMS_ITEM_PARTICLES", itemParticles.toString()));
			} else {
				target.removeTag("MMOITEMS_ITEM_PARTICLES");
			}

			ItemStack targetItem = target.toItem();
			ItemMeta targetItemMeta = targetItem.getItemMeta();
			ItemMeta originalItemMeta = originalItem.getItemMeta();

			if(targetItemMeta.isUnbreakable()) {
				targetItemMeta.setUnbreakable(originalItemMeta.isUnbreakable());
				if(targetItemMeta instanceof Damageable && originalItemMeta instanceof Damageable)
					((Damageable) targetItemMeta).setDamage(((Damageable) originalItemMeta).getDamage());
			}

			if(targetItemMeta instanceof LeatherArmorMeta && originalItemMeta instanceof LeatherArmorMeta)
				((LeatherArmorMeta) targetItemMeta).setColor(((LeatherArmorMeta) originalItemMeta).getColor());

			if (target.hasTag("SkullOwner") && (targetItem.getType() == VersionMaterial.PLAYER_HEAD.toMaterial())
					&& (originalItem.getType() == VersionMaterial.PLAYER_HEAD.toMaterial())) {
				try {
					Field profileField = targetItemMeta.getClass().getDeclaredField("profile");
					profileField.setAccessible(true);
					profileField.set(targetItemMeta,
							((SkullTextureData) originalMmoitem.getData(ItemStats.SKULL_TEXTURE)).getGameProfile());
				} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
					MMOItems.plugin.getLogger().warning("Could not read skull texture");
				}
			}

			targetItem.setItemMeta(targetItemMeta);
			targetItem.setType(originalItem.getType());
			event.getCurrentItem().setAmount(0);
			new SmartGive(player).give(targetItem);


			// Give back skin item
			MMOItemTemplate template = MMOItems.plugin.getTemplates().getTemplateOrThrow(Type.SKIN, skinId);
			MMOItem mmoitem = template.newBuilder(playerData.getRPG()).build();
			ItemStack item = mmoitem.newBuilder().build();

			new SmartGive(player).give(item);
			Message.SKIN_REMOVED
					.format(ChatColor.YELLOW, "#item#", MMOUtils.getDisplayName(targetItem))
					.send(player);
			return true;
		}

		return false;
	}

	/**
	 * @return If the item should be consumed
	 */
	public boolean useWithoutItem() {
		NBTItem nbtItem = getNBTItem();

		if (nbtItem.getBoolean("MMOITEMS_INEDIBLE"))
			return false;

		double health = nbtItem.getStat("RESTORE_HEALTH");
		if (health > 0)
			MMOUtils.heal(player, health);

		double food = nbtItem.getStat("RESTORE_FOOD");
		if (food > 0)
			MMOUtils.feed(player, (int) food);

		double saturation = nbtItem.getStat("RESTORE_SATURATION");
		saturation = saturation == 0 ? 6 : saturation;
		if (saturation > 0)
			MMOUtils.saturate(player, (float) saturation);

		double mana = nbtItem.getStat("RESTORE_MANA");
		if (mana > 0)
			playerData.getRPG().giveMana(mana);

		double stamina = nbtItem.getStat("RESTORE_STAMINA");
		if (stamina > 0)
			playerData.getRPG().giveStamina(stamina);

		// potion effects
		if (mmoitem.hasData(ItemStats.EFFECTS))
			((PotionEffectListData) mmoitem.getData(ItemStats.EFFECTS)).getEffects().forEach(effect -> {
				player.removePotionEffect(effect.getType());
				player.addPotionEffect(effect.toEffect());
			});

		if (nbtItem.hasTag("MMOITEMS_SOUND_ON_CONSUME"))
			player.getWorld().playSound(player.getLocation(), nbtItem.getString("MMOITEMS_SOUND_ON_CONSUME").toLowerCase(),
					(float) nbtItem.getDouble("MMOITEMS_SOUND_ON_CONSUME_VOL"), (float) nbtItem.getDouble("MMOITEMS_SOUND_ON_CONSUME_PIT"));
		else
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1, 1);

		int maxConsume = (int) nbtItem.getStat("MAX_CONSUME");
		if (maxConsume > 1) {
			ItemStack item = nbtItem.toItem().clone();
			String configMaxConsumeLore = MMOLib.plugin.parseColors(MMOItems.plugin.getLanguage().getStatFormat("max-consume"));
			String maxConsumeLore = configMaxConsumeLore.replace("#", Integer.toString(maxConsume));

			maxConsume -= 1;
			nbtItem.addTag(new ItemTag("MMOITEMS_MAX_CONSUME", maxConsume));

			ItemStack usedItem = nbtItem.toItem().clone();
			usedItem.setAmount(1);

			ItemMeta usedItemMeta = usedItem.getItemMeta();
			List<String> itemLores = usedItemMeta.getLore();

			for (int i = 0; i < itemLores.size(); i++) {
				if (itemLores.get(i).equals(maxConsumeLore)) {
					maxConsumeLore = configMaxConsumeLore.replace("#", Integer.toString(maxConsume));
					itemLores.set(i, maxConsumeLore);

					usedItemMeta.setLore(itemLores);
					usedItem.setItemMeta(usedItemMeta);

					break;
				}
			}

			if (player.getInventory().getItemInMainHand().equals(item))
				player.getInventory().setItemInMainHand(usedItem);
			else if (player.getInventory().getItemInOffHand().equals(item))
				player.getInventory().setItemInOffHand(usedItem);

			if (item.getAmount() > 1) {
				item.setAmount(item.getAmount() - 1);
				new SmartGive(player).give(item);
			}

			return false;
		}

		return !nbtItem.getBoolean("MMOITEMS_DISABLE_RIGHT_CLICK_CONSUME");
	}

	public boolean hasVanillaEating() {
		return (getItem().getType().isEdible() || getItem().getType() == Material.POTION || getItem().getType() == Material.MILK_BUCKET)
				&& getNBTItem().hasTag("MMOITEMS_VANILLA_EATING");
	}
}
