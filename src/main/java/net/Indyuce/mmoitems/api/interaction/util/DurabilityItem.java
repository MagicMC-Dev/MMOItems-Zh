package net.Indyuce.mmoitems.api.interaction.util;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.api.event.item.CustomDurabilityDamage;
import net.Indyuce.mmoitems.api.event.item.CustomDurabilityRepair;
import net.Indyuce.mmoitems.api.item.util.DynamicLore;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class DurabilityItem {
	private final NBTItem nbtItem;
	private final Player player;
	private int maxDurability , unbreakingLevel;

	private int durability;

	private static final Random random = new Random();

	/**
	 * Use to handle durability changes for MMOItems without using heavy MMOItem
	 * class methods
	 * 
	 * @param player Player holding the item
	 * @param item   Item with durability
	 */
	public DurabilityItem(Player player, ItemStack item) {
		this(player, MythicLib.plugin.getVersion().getWrapper().getNBTItem(item));
	}

	/**
	 * Use to handle durability changes for MMOItems without using heavy MMOItem
	 * class methods
	 * 
	 * @param player Player holding the item
	 * @param item   Item with durability
	 */
	public DurabilityItem(Player player, NBTItem item) {


		this.player = player;
		this.nbtItem = item;


		durability = nbtItem.getInteger("MMOITEMS_DURABILITY");
		maxDurability = nbtItem.getInteger("MMOITEMS_MAX_DURABILITY");
		unbreakingLevel = nbtItem.getItem().getItemMeta().getEnchantLevel(Enchantment.DURABILITY);
	}

	public Player getPlayer() {
		return player;
	}

	public int getMaxDurability() {
		return maxDurability;
	}

	public int getDurability() {
		return durability;
	}

	public int getUnbreakingLevel() {
		return unbreakingLevel;
	}

	public NBTItem getNBTItem() {
		return nbtItem;
	}

	public boolean isBroken() {
		return nbtItem.hasTag("MMOITEMS_DURABILITY") && durability <= 0;
	}

	public boolean isLostWhenBroken() {
		return nbtItem.getBoolean("MMOITEMS_WILL_BREAK");
	}

	public boolean isValid() {
		return player.getGameMode() != GameMode.CREATIVE && nbtItem.hasTag("MMOITEMS_DURABILITY");
	}

	public DurabilityItem addDurability(int gain) {
		CustomDurabilityRepair evG = new CustomDurabilityRepair(this, gain);
		Bukkit.getPluginManager().callEvent(evG);
		if (evG.isCancelled()) { return this; }

		Validate.isTrue(gain > 0, "Durability gain must be greater than 0");
		durability = Math.max(0, Math.min(durability + gain, maxDurability));
		return this;
	}

	public DurabilityItem decreaseDurability(int loss) {
		CustomDurabilityDamage evG = new CustomDurabilityDamage(this, loss);
		Bukkit.getPluginManager().callEvent(evG);
		if (evG.isCancelled()) { return this; }

		/*
		 * Calculate the chance of the item not losing any durability because of
		 * the vanilla unbreaking enchantment ; an item with unbreaking X has 1
		 * 1 chance out of (X + 1) to lose a durability point, that's 50% chance
		 * -> 33% chance -> 25% chance -> 20% chance...
		 */
		if (getUnbreakingLevel() > 0 && random.nextInt(getUnbreakingLevel()) > 0)
			return this;

		durability = Math.max(0, Math.min(durability - loss, maxDurability));

		// When the item breaks
		if (durability <= 0) {
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
			PlayerData.get(player).getInventory().scheduleUpdate();
		}

		return this;
	}

	public ItemStack toItem() {

		/*
		 * Cross multiplication to display the current item durability on the
		 * item durability bar. (1 - ratio) because minecraft works with item
		 * damage, and item damage is the complementary of the remaining
		 * durability.
		 * 
		 * Make sure the vanilla bar displays at least 1 damage for display
		 * issues. Also makes sure the item can be mended using the vanilla
		 * enchant.
		 */
		int damage = durability == maxDurability ? 0
				: Math.max(1, (int) ((1. - ((double) durability / maxDurability)) * nbtItem.getItem().getType().getMaxDurability()));
		nbtItem.addTag(new ItemTag("MMOITEMS_DURABILITY", durability), new ItemTag("Damage", damage));

		return new DynamicLore(nbtItem).build();
	}
}
