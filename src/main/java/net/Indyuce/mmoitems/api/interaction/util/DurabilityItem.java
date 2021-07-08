package net.Indyuce.mmoitems.api.interaction.util;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.event.item.CustomDurabilityDamage;
import net.Indyuce.mmoitems.api.event.item.CustomDurabilityRepair;
import net.Indyuce.mmoitems.api.item.util.LoreUpdate;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

/**
 * Class which handles custom durability; you can add or remove
 * some durability from an item and generate the new version.
 * <p>
 * This does update the item lore dynamically. However due to the current
 * implementation of {@link LoreUpdate}, if other plugins edit the line
 * corresponding to durability, MMOItems won't be able to detect it again.
 *
 * @author indyuce
 */
public class DurabilityItem {
	private final NBTItem nbtItem;
	@Nullable
	private final Player player;
	private final int maxDurability, unbreakingLevel, initialDurability;
	private final boolean barHidden;

	private int durability;

	private static final Random RANDOM = new Random();

	/**
	 * Use to handle durability changes for MMOItems
	 * without using heavy MMOItem class methods.
	 *
	 * @param player Player holding the item
	 * @param item   Item with durability
	 */
	public DurabilityItem(@Nullable Player player, @NotNull ItemStack item) {
		this(player, NBTItem.get(item));
	}

	/**
	 * Use to handle durability changes for MMOItems
	 * without using heavy MMOItem class methods
	 *
	 * @param player Player holding the item
	 * @param item   Item with durability
	 */
	public DurabilityItem(@Nullable Player player, @NotNull NBTItem item) {
		/*Validate.notNull(this.player = player, "Player cannot be null");*/
		this.player = player;
		Validate.notNull(nbtItem = item, "Item cannot be null");

		maxDurability = nbtItem.getInteger("MMOITEMS_MAX_DURABILITY");
		initialDurability = durability = nbtItem.hasTag("MMOITEMS_DURABILITY") ? nbtItem.getInteger("MMOITEMS_DURABILITY") : maxDurability;
		barHidden = nbtItem.getBoolean("MMOITEMS_DURABILITY_BAR");

		unbreakingLevel = (nbtItem.getItem().getItemMeta() != null && nbtItem.getItem().getItemMeta().hasEnchant(Enchantment.DURABILITY)) ?
				nbtItem.getItem().getItemMeta().getEnchantLevel(Enchantment.DURABILITY) :
				0;
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

	// If the green vanilla durability bar should show
	public boolean isBarHidden() {
		return barHidden;
	}

	/**
	 * @deprecated Not used anymore
	 */
	@Deprecated
	public boolean isUnbreakable() {
		return nbtItem.getBoolean("Unbreakable");
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

	/**
	 * Since
	 *
	 * @return If the item actually supports custom durability.
	 */
	public boolean isValid() {
		return maxDurability > 0 && player.getGameMode() != GameMode.CREATIVE;
	}

	public DurabilityItem addDurability(int gain) {
		Validate.isTrue(gain > 0, "Durability gain must be greater than 0");

		CustomDurabilityRepair event = new CustomDurabilityRepair(this, gain);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled())
			return this;

		durability = Math.min(durability + gain, maxDurability);
		return this;
	}

	public DurabilityItem decreaseDurability(int loss) {
		CustomDurabilityDamage event = new CustomDurabilityDamage(this, loss);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled())
			return this;

		/*
		 * Calculate the chance of the item not losing any durability because of
		 * the vanilla unbreaking enchantment ; an item with unbreaking X has 1
		 * 1 chance out of (X + 1) to lose a durability point, that's 50% chance
		 * -> 33% chance -> 25% chance -> 20% chance...
		 */
		if (getUnbreakingLevel() > 0 && RANDOM.nextInt(getUnbreakingLevel()) > 0)
			return this;

		durability = Math.max(0, Math.min(durability - loss, maxDurability));

		// When the item breaks
		if (durability <= 0 && player != null) {
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
			PlayerData.get(player).getInventory().scheduleUpdate();
		}

		return this;
	}

	public ItemStack toItem() {

		// No modification needs to be done
		if (durability == initialDurability)
			return nbtItem.getItem();

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
		if (!barHidden) {
			int damage = (durability == maxDurability) ? 0
					: Math.max(1, (int) ((1. - ((double) durability / maxDurability)) * nbtItem.getItem().getType().getMaxDurability()));
			nbtItem.addTag(new ItemTag("Damage", damage));
		}

		nbtItem.addTag(new ItemTag("MMOITEMS_DURABILITY", durability));

		// Apply the NBT tags
		ItemStack item = nbtItem.toItem();

		// Item lore update
		String format = MythicLib.inst().parseColors(MMOItems.plugin.getLanguage().getStatFormat("durability").replace("#m", "" + maxDurability));
		String old = format.replace("#c", "" + initialDurability);
		String replaced = format.replace("#c", "" + durability);
		return new LoreUpdate(item, old, replaced).updateLore();
	}
}
