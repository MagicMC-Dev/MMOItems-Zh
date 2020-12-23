package net.Indyuce.mmoitems.api.interaction;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.comp.flags.FlagPlugin.CustomFlag;
import net.Indyuce.mmoitems.stat.data.PotionEffectListData;
import net.Indyuce.mmoitems.stat.type.ConsumableItemInteraction;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.item.NBTItem;
import net.mmogroup.mmolib.api.util.SmartGive;

public class Consumable extends UseItem {
	public Consumable(Player player, NBTItem item) {
		super(player, item);
	}

	@Override
	public boolean applyItemCosts() {
		return MMOItems.plugin.getFlags().isFlagAllowed(player, CustomFlag.MI_CONSUMABLES) && playerData.getRPG().canUse(getNBTItem(), true);
	}

	/**
	 * Applies a consumable onto an item
	 * 
	 * @param  event  The click event
	 * @param  target The item on which the consumable is being applied
	 * @return        If the consumable was successfully applied on the item
	 */
	public boolean useOnItem(InventoryClickEvent event, NBTItem target) {
		if (event.getClickedInventory() != event.getWhoClicked().getInventory())
			return false;

		Type targetType = Type.get(target.getType());
		for (ConsumableItemInteraction action : MMOItems.plugin.getStats().getConsumableActions())
			if (action.handleConsumableEffect(event, playerData, this, target, targetType))
				return true;

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
