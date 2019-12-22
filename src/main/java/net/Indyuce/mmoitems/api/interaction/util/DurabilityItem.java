package net.Indyuce.mmoitems.api.interaction.util;

import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.event.ItemBreakEvent;
import net.Indyuce.mmoitems.api.event.ItemLoseDurabilityEvent;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.item.NBTItem;

public class DurabilityItem {
	private final NBTItem nbtItem;
	private final Player player;
	
	private int unbreakingLevel = -1, durability, maxDurability = -1;
	private boolean broken;

	private static final Random random = new Random();

	public DurabilityItem(Player player, ItemStack item) {
		this(player, MMOLib.plugin.getNMS().getNBTItem(item));
	}

	/*
	 * durability loss is not perfect and thus should only be used with weapons
	 * and not with tools which could lose durability more than often e.g when
	 * breaking a block with shears
	 */
	public DurabilityItem(Player player, NBTItem item) {
		this.player = player;
		this.nbtItem = item;
		this.durability = nbtItem.getInteger("MMOITEMS_DURABILITY");
	}

	public int getMaxDurability() {
		return maxDurability < 0 ? maxDurability = nbtItem.getInteger("MMOITEMS_MAX_DURABILITY") : maxDurability;
	}

	public int getDurability() {
		return durability;
	}

	public int getUnbreakingLevel() {
		return unbreakingLevel < 0 ? nbtItem.getItem().getItemMeta().getEnchantLevel(Enchantment.DURABILITY) : unbreakingLevel;
	}

	public NBTItem getNBTItem() {
		return nbtItem;
	}

	public boolean isValid() {
		return player.getGameMode() != GameMode.CREATIVE && nbtItem.hasTag("MMOITEMS_DURABILITY");
	}

	public DurabilityItem addDurability(int gain) {
		durability = Math.max(0, Math.min(durability + gain, getMaxDurability()));
		return this;
	}

	public DurabilityItem doBreak() {
		broken = true;
		return this;
	}

	public DurabilityItem decreaseDurability(int loss) {

		/*
		 * calculate the chance of the item not losing any durability because of
		 * the vanilla unbreaking enchantment ; an item with unbreaking X has 1
		 * 1 chance out of (X + 1) to lose a durability point, that's 50% chance
		 * -> 33% chance -> 25% chance -> 20% chance...
		 */
		if (getUnbreakingLevel() > 0 && random.nextInt(getUnbreakingLevel()) > 0)
			return this;

		ItemLoseDurabilityEvent event = new ItemLoseDurabilityEvent(player, nbtItem, durability, loss);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled())
			return this;

		addDurability(-loss);

		// when the item breaks
		if (durability < 1) {
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
			ItemBreakEvent breakEvent = new ItemBreakEvent(player, nbtItem);
			Bukkit.getPluginManager().callEvent(breakEvent);

			/*
			 * does NOT work with armors. armors need to be entirely updated
			 * when they break. no problem since they do not have any dupe issue
			 */
			player.getInventory().removeItem(nbtItem.getItem());

			/*
			 * when the item breaks and gets really removed from the player
			 * inventory since the corresponding item option is toggled on
			 */
			if (breakEvent.doesItemBreak()) {
				Message.ITEM_BROKE.format(ChatColor.RED, "#item#", MMOUtils.getDisplayName(nbtItem.getItem())).send(player, "item-break");
				doBreak();
				return this;
			}

			/*
			 * when the item is unusable, it gets removed from the player
			 * inventory but gets added again in a different slot to make sure
			 * the player unequips it and won't be able to equip it again
			 * without repairing it first
			 */
			for (ItemStack drop : player.getInventory().addItem(toItem()).values())
				player.getWorld().dropItem(player.getLocation(), drop);
			doBreak();

			Message.ZERO_DURABILITY.format(ChatColor.RED).send(player, "item-break");
			PlayerData.get(player).updateInventory();
			return this;
		}

		return this;
	}

	public ItemStack toItem() {

		/*
		 * is the item is broken, return a null. this is used by armors to set
		 * the armor piece to null since it's not removed from the inventory
		 */
		if (broken)
			return null;

		/*
		 * calculate the new durability state and update it in the item lore. if
		 * the durability state is null, it either means the durability state is
		 * out of the lore format or the state display was changed, thus in both
		 * cases it shall no be updated
		 */
		nbtItem.addTag(new ItemTag("MMOITEMS_DURABILITY", durability));
		DurabilityState state = getDurabilityState();
		if (state == null)
			return nbtItem.toItem();

		/*
		 * if the item does not have the correct durability state, update it
		 */
		DurabilityState expected = getExpectedDurabilityState();
		if (!state.equals(expected)) {
			List<String> lore = nbtItem.getItem().getItemMeta().getLore();
			for (int j = 0; j < lore.size(); j++)
				if (lore.get(j).equals(state.getDisplay())) {
					nbtItem.addTag(new ItemTag("MMOITEMS_DURABILITY_STATE", expected.getID())).toItem();

					ItemStack result = nbtItem.toItem();
					ItemMeta meta = result.getItemMeta();
					lore.set(j, expected.getDisplay());
					meta.setLore(lore);
					result.setItemMeta(meta);
					return result;
				}
		}

		return nbtItem.toItem();
	}

	private DurabilityState getDurabilityState() {
		String tag = nbtItem.getString("MMOITEMS_DURABILITY_STATE");
		return MMOItems.plugin.getLanguage().hasDurabilityState(tag) ? MMOItems.plugin.getLanguage().getDurabilityState(tag) : null;
	}

	private DurabilityState getExpectedDurabilityState() {
		return getExpectedDurabilityState(durability, getMaxDurability());
	}

	// used during item generation to determine the item first state
	public static DurabilityState getExpectedDurabilityState(int durability, int max) {
		for (DurabilityState state : MMOItems.plugin.getLanguage().getDurabilityStates())
			if (state.isInState(durability, max))
				return state;
		return null;
	}
}
