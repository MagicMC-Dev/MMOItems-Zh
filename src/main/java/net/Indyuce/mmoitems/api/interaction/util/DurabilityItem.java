package net.Indyuce.mmoitems.api.interaction.util;

import java.util.Random;

import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmoitems.api.player.PlayerData;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.item.NBTItem;

public class DurabilityItem {
	private final NBTItem nbtItem;
	private final Player player;
	private final int maxDurability, unbreakingLevel;

	/*
	 * broken if below than 0
	 */
	private int durability;

	private static final Random random = new Random();

	public DurabilityItem(Player player, ItemStack item) {
		this(player, MMOLib.plugin.getVersion().getWrapper().getNBTItem(item));
	}

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
		return durability <= 0;
	}

	public boolean isLostWhenBroken() {
		return nbtItem.getBoolean("MMOITEMS_WILL_BREAK");
	}

	public boolean isValid() {
		return player.getGameMode() != GameMode.CREATIVE && nbtItem.hasTag("MMOITEMS_DURABILITY");
	}

	public DurabilityItem addDurability(int gain) {
		durability = Math.max(0, Math.min(durability + gain, getMaxDurability()));
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

		addDurability(-loss);

		// when the item breaks
		if (durability <= 0) {
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
			PlayerData.get(player).scheduleDelayedInventoryUpdate();
		}

		return this;
	}

	public ItemStack toItem() {

		/*
		 * calculate the new durability state and update it in the item lore. if
		 * the durability state is null, it either means the durability state is
		 * out of the lore format or the state display was changed, thus in both
		 * cases it shall no be updated
		 */
		nbtItem.addTag(new ItemTag("MMOITEMS_DURABILITY", durability));
		ItemStack item = nbtItem.toItem();

		/*
		 * update vanilla durability
		 */
		double ratio = (double) durability / maxDurability;
		int damage = (int) ((1. - ratio) * item.getType().getMaxDurability());

		/*
		 * make sure the vanilla bar displays at least 1 damage so the item can
		 * always be mended
		 */
		damage = Math.max(ratio < 1 ? 1 : 0, damage);

		ItemMeta meta = item.getItemMeta();
		MMOLib.plugin.getVersion().getWrapper().applyDurability(item, meta, damage);
		item.setItemMeta(meta);

		return item;
	}
}
