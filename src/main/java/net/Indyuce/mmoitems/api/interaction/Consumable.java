package net.Indyuce.mmoitems.api.interaction;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.interaction.util.DurabilityItem;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.plugin.identify.IdentifiedItem;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.comp.flags.FlagPlugin.CustomFlag;
import net.Indyuce.mmoitems.stat.Soulbound.SoulboundData;
import net.Indyuce.mmoitems.stat.Upgrade_Stat.UpgradeData;
import net.Indyuce.mmoitems.stat.data.EffectListData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.item.NBTItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class Consumable extends UseItem {
	public Consumable(Player player, NBTItem item, Type type) {
		super(player, item, type);
	}

	@Override
	public boolean canBeUsed() {
		return MMOItems.plugin.getFlags().isFlagAllowed(player, CustomFlag.MI_CONSUMABLES) && playerData.getRPG().canUse(getNBTItem(), true);
	}

	/*
	 * this boolean is used to check if the consumable has applied at least once
	 * of its item options. if so, the consumable should be consumed
	 */
	public boolean useOnItem(InventoryClickEvent event, NBTItem target) {
		if (event.getClickedInventory() != event.getWhoClicked().getInventory())
			return false;

		/*
		 * unidentified items do not have any type, so you must check if the
		 * item has a type first.
		 */
		Type targetType = target.getType();
		if (targetType == null) {
			String unidentifiedItemTag = target.getString("MMOITEMS_UNIDENTIFIED_ITEM");
			if (getNBTItem().getBoolean("MMOITEMS_CAN_IDENTIFY") && !unidentifiedItemTag.equals("")) {
				event.setCurrentItem(new IdentifiedItem(target).identify());
				Message.SUCCESSFULLY_IDENTIFIED.format(ChatColor.YELLOW, "#item#", MMOUtils.getDisplayName(event.getCurrentItem())).send(player);
				player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
				return true;
			}
			return false;
		}

		/*
		 * deconstructing an item. usually consumables do not deconstruct and
		 * repair items at the same time so there's no pb with that
		 */
		String itemTierTag = target.getString("MMOITEMS_TIER");
		if (getNBTItem().getBoolean("MMOITEMS_CAN_DECONSTRUCT") && !itemTierTag.equals("")) {
			ItemTier tier = MMOItems.plugin.getTiers().get(itemTierTag);
			List<ItemStack> deconstructed = tier.generateDeconstructedItem();
			if (!deconstructed.isEmpty()) {
				Message.SUCCESSFULLY_DECONSTRUCTED.format(ChatColor.YELLOW, "#item#", MMOUtils.getDisplayName(event.getCurrentItem())).send(player);
				event.getCurrentItem().setAmount(event.getCurrentItem().getAmount() - 1);
				for (ItemStack drop : player.getInventory().addItem(deconstructed.toArray(new ItemStack[deconstructed.size()])).values())
					player.getWorld().dropItem(player.getLocation(), drop);
				player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
			}

			return true;
		}

		/*
		 * upgrading an item. it is better not to repair an item while upgrading
		 * it.
		 */
		if (getNBTItem().hasTag("MMOITEMS_UPGRADE") && target.hasTag("MMOITEMS_UPGRADE")) {
			if (target.getItem().getAmount() > 1) {
				Message.CANT_UPGRADED_STACK.format(ChatColor.RED).send(player);
				player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
				return false;
			}

			MMOItem targetMMO = new MMOItem(target);
			UpgradeData targetSharpening = (UpgradeData) targetMMO.getData(ItemStat.UPGRADE);
			if (!targetSharpening.canLevelUp()) {
				Message.MAX_UPGRADES_HIT.format(ChatColor.RED).send(player);
				player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
				return false;
			}

			UpgradeData consumableSharpening = (UpgradeData) mmoitem.getData(ItemStat.UPGRADE);
			if (!consumableSharpening.matchesReference(targetSharpening)) {
				Message.WRONG_UPGRADE_REFERENCE.format(ChatColor.RED).send(player);
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

			targetSharpening.upgrade(targetMMO);
			Message.UPGRADE_SUCCESS.format(ChatColor.YELLOW, "#item#", MMOUtils.getDisplayName(event.getCurrentItem())).send(player);
			event.getCurrentItem().setItemMeta(targetMMO.newBuilder().build().getItemMeta());
			player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
			return true;
		}

		/*
		 * applying a soulbound onto an item. it does not work if the item
		 * already has a soulbound, and it has a chance to successfully apply.
		 */
		double soulbindingChance = getNBTItem().getStat(ItemStat.SOULBINDING_CHANCE);
		if (soulbindingChance > 0) {
			if (target.getItem().getAmount() > 1) {
				Message.CANT_BIND_STACKED.format(ChatColor.RED).send(player, "soulbound");
				return false;
			}

			MMOItem targetMMO = new MMOItem(target, false);
			if (targetMMO.hasData(ItemStat.SOULBOUND)) {
				SoulboundData data = (SoulboundData) targetMMO.getData(ItemStat.SOULBOUND);
				Message.CANT_BIND_ITEM.format(ChatColor.RED, "#player#", data.getName(), "#level#", MMOUtils.intToRoman(data.getLevel())).send(player, "soulbound");
				return false;
			}

			if (random.nextDouble() < soulbindingChance / 100) {
				int soulboundLevel = (int) Math.max(1, getNBTItem().getStat(ItemStat.SOULBOUND_LEVEL));
				(targetMMO = new MMOItem(target)).setData(ItemStat.SOULBOUND, ItemStat.SOULBOUND.newSoulboundData(player.getUniqueId(), player.getName(), soulboundLevel));
				target.getItem().setItemMeta(targetMMO.newBuilder().build().getItemMeta());
				Message.SUCCESSFULLY_BIND_ITEM.format(ChatColor.YELLOW, "#item#", MMOUtils.getDisplayName(target.getItem()), "#level#", MMOUtils.intToRoman(soulboundLevel)).send(player, "soulbound");
				player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
				return true;
			}

			Message.UNSUCCESSFUL_SOULBOUND.format(ChatColor.RED).send(player, "soulbound");
			player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
			return true;
		}

		/*
		 * breaking the item's current soulbound. it has a random factor
		 * determined by the soulbound break chance, and the consumable needs to
		 * have at least the soulbound's level to be able to break the item
		 * soulbound.
		 */
		double soulboundBreakChance = getNBTItem().getStat(ItemStat.SOULBOUND_BREAK_CHANCE);
		if (soulboundBreakChance > 0) {
			MMOItem targetMMO = new MMOItem(target, false);
			if (!targetMMO.hasData(ItemStat.SOULBOUND)) {
				Message.NO_SOULBOUND.format(ChatColor.RED).send(player, "soulbound");
				player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
				return false;
			}

			SoulboundData soulbound = (SoulboundData) targetMMO.getData(ItemStat.SOULBOUND);

			// check for soulbound level
			if (Math.max(1, getNBTItem().getStat(ItemStat.SOULBOUND_LEVEL)) < soulbound.getLevel()) {
				Message.LOW_SOULBOUND_LEVEL.format(ChatColor.RED, "#level#", MMOUtils.intToRoman(soulbound.getLevel())).send(player, "soulbound");
				return false;
			}

			if (random.nextDouble() < soulboundBreakChance / 100) {
				(targetMMO = new MMOItem(target)).removeData(ItemStat.SOULBOUND);
				target.getItem().setItemMeta(targetMMO.newBuilder().build().getItemMeta());
				Message.SUCCESSFULLY_BREAK_BIND.format(ChatColor.YELLOW, "#level#", MMOUtils.intToRoman(soulbound.getLevel())).send(player, "soulbound");
				player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 2);
			} else {
				Message.UNSUCCESSFUL_SOULBOUND_BREAK.format(ChatColor.RED).send(player, "soulbound");
				player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 0);
			}

			return true;
		}

		/*
		 * item repairing, does not apply if there's no repair power or if the
		 * item still has all its uses left
		 */
		int repairPower = (int) getNBTItem().getStat(ItemStat.REPAIR);
		if (repairPower > 0) {

			// custom durability
			if (target.hasTag("MMOITEMS_DURABILITY")) {
				DurabilityItem durItem = new DurabilityItem(player, target);
				if (durItem.getDurability() < durItem.getMaxDurability()) {
					target.getItem().setItemMeta(durItem.addDurability(repairPower).toItem().getItemMeta());
					Message.REPAIRED_ITEM.format(ChatColor.YELLOW, "#item#", MMOUtils.getDisplayName(target.getItem()), "#amount#", "" + repairPower).send(player);
				}
				return true;
			}

			// vanilla durability
			if (!target.getBoolean("Unbreakable") && MMOLib.plugin.getVersion().getWrapper().isDamaged(target.getItem(), target.getItem().getItemMeta())) {
				MMOLib.plugin.getVersion().getWrapper().repair(target.getItem(), repairPower);
				Message.REPAIRED_ITEM.format(ChatColor.YELLOW, "#item#", MMOUtils.getDisplayName(target.getItem()), "#amount#", "" + repairPower).send(player);
				return true;
			}
		}

		return false;
	}

	/*
	 * when the method returns true, one item will be taken away from the player
	 * inventory
	 */
	public boolean useWithoutItem(boolean consume) {
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
		if (mmoitem.hasData(ItemStat.EFFECTS))
			((EffectListData) mmoitem.getData(ItemStat.EFFECTS)).getEffects().forEach(effect -> {
				player.removePotionEffect(effect.getType());
				player.addPotionEffect(effect.toEffect());
			});

		if (nbtItem.hasTag("MMOITEMS_SOUND_ON_CONSUME"))
			player.getWorld().playSound(player.getLocation(), nbtItem.getString("MMOITEMS_SOUND_ON_CONSUME"), (float) nbtItem.getDouble("MMOITEMS_SOUND_ON_CONSUME_VOL"), (float) nbtItem.getDouble("MMOITEMS_SOUND_ON_CONSUME_PIT"));
		else
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1, 1);

		int maxConsume = (int) nbtItem.getStat("MAX_CONSUME");
		if (maxConsume > 1) {
			ItemStack item = nbtItem.toItem().clone();
			String configMaxConsumeLore = ChatColor.translateAlternateColorCodes('&', MMOItems.plugin.getLanguage().getStatFormat("max-consume"));
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
				MMOUtils.giveOrDrop(player, item);
			}

			return false;
		}

		return consume && !nbtItem.getBoolean("MMOITEMS_DISABLE_RIGHT_CLICK_CONSUME");
	}

	public boolean hasVanillaEating() {
		return (getItem().getType().isEdible() || getItem().getType() == Material.POTION || getItem().getType() == Material.MILK_BUCKET) && getNBTItem().hasTag("MMOITEMS_VANILLA_EATING");
	}
}
