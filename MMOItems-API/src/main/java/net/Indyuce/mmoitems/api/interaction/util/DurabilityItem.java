package net.Indyuce.mmoitems.api.interaction.util;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.event.item.CustomDurabilityDamage;
import net.Indyuce.mmoitems.api.event.item.ItemCustomRepairEvent;
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem;
import net.Indyuce.mmoitems.api.item.util.LoreUpdate;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.UpgradeData;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
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
    private final int maxDurability, unbreakingLevel, initialDurability;
    private final boolean barHidden;
    private final Player player;

    private int durability;

    private static final Random RANDOM = new Random();

    /**
     * Use to handle durability changes for MMOItems
     * without using heavy MMOItem class methods.
     *
     * @param player Player holding the item
     * @param item   Item with durability
     */
    public DurabilityItem(@NotNull Player player, @NotNull ItemStack item) {
        this(player, NBTItem.get(item));
    }

    /**
     * Use to handle durability changes for MMOItems
     * without using heavy MMOItem class methods
     *
     * @param player  Player holding the item
     * @param nbtItem Item with durability
     */
    public DurabilityItem(@NotNull Player player, @NotNull NBTItem nbtItem) {
        this.player = Objects.requireNonNull(player, "Player cannot be null");
        this.nbtItem = Objects.requireNonNull(nbtItem, "Item cannot be null");

        maxDurability = nbtItem.getInteger("MMOITEMS_MAX_DURABILITY");
        initialDurability = durability = nbtItem.hasTag("MMOITEMS_DURABILITY") ? nbtItem.getInteger("MMOITEMS_DURABILITY") : maxDurability;
        barHidden = nbtItem.getBoolean("MMOITEMS_DURABILITY_BAR");

        unbreakingLevel = (nbtItem.getItem().getItemMeta() != null && nbtItem.getItem().getItemMeta().hasEnchant(Enchantment.DURABILITY)) ?
                nbtItem.getItem().getItemMeta().getEnchantLevel(Enchantment.DURABILITY) : 0;
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

    public boolean isBarHidden() {
        return barHidden;
    }

    public int getUnbreakingLevel() {
        return unbreakingLevel;
    }

    public NBTItem getNBTItem() {
        return nbtItem;
    }

    /**
     * @return If both this is a VALID custom durability item and if the item is broken.
     *         This will return <code>false</code> if it is not a valid item
     */
    public boolean isBroken() {
        return maxDurability > 0 && durability <= 0;
    }

    public boolean isLostWhenBroken() {
        return nbtItem.getBoolean("MMOITEMS_WILL_BREAK");
    }

    public boolean isDowngradedWhenBroken() {
        return nbtItem.getBoolean("MMOITEMS_BREAK_DOWNGRADE");
    }

    /**
     * @return If the item actually supports custom durability. It is completely
     *         disabled when the player is in creative mode just like vanilla durability.
     */
    public boolean isValid() {
        return maxDurability > 0 && player.getGameMode() != GameMode.CREATIVE;
    }

    @NotNull
    public DurabilityItem addDurability(int gain) {
        Validate.isTrue(gain > 0, "Durability gain must be greater than 0");

        ItemCustomRepairEvent event = new ItemCustomRepairEvent(this, gain);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return this;

        durability = Math.min(durability + gain, maxDurability);
        return this;
    }

    public DurabilityItem decreaseDurability(int loss) {

        // This happens when Unbreaking applies for a damageable item
        if (loss == 0)
            return this;

        /*
         * Calculate the chance of the item not losing any durability because of
         * the vanilla unbreaking enchantment ; an item with unbreaking X has 1
         * 1 chance out of (X + 1) to lose a durability point, that's 50% chance
         * -> 33% chance -> 25% chance -> 20% chance...
         *
         * This should only be taken into account if the item being damaged is
         * UNDAMAGEABLE since Bukkit already applies the enchant for damageable items.
         */
        if (nbtItem.getItem().getType().getMaxDurability() == 0) {
            final int unbreakingLevel = getUnbreakingLevel();
            if (unbreakingLevel > 0 && RANDOM.nextInt(unbreakingLevel + 1) != 0)
                return this;
        }

        CustomDurabilityDamage event = new CustomDurabilityDamage(this, loss);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return this;

        durability = Math.max(0, Math.min(durability - loss, maxDurability));

        // Play sound when item breaks
        if (isBroken()) {
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
            PlayerData.get(player).getInventory().scheduleUpdate();
        }

        return this;
    }

    /**
     * This method not only generates the newest item version taking into account
     * 1) item damage
     * 2) item breaking
     * 3) item downgrade
     *
     * @return Newest version of the damaged item.
     *         <code>null</code> if the item breaks. That method CANNOT
     *         return a null value if the item has no decreased its durability.
     */
    @Nullable
    public ItemStack toItem() {

        if (isBroken()) {

            // Lost when broken
            if (isLostWhenBroken())
                return null;

            // Checks for possible downgrade
            if (isDowngradedWhenBroken()) {
                ItemTag uTag = ItemTag.getTagAtPath(ItemStats.UPGRADE.getNBTPath(), getNBTItem(), SupportedNBTTagValues.STRING);
                if (uTag != null)
                    try {
                        UpgradeData data = new UpgradeData(new JsonParser().parse((String) uTag.getValue()).getAsJsonObject());

                        // If it cannot be downgraded (reached min), DEATH
                        if (data.getLevel() <= data.getMin())
                            return null;

                        // Remove one level and FULLY repair item
                        LiveMMOItem mmo = new LiveMMOItem(getNBTItem());
                        mmo.setData(ItemStats.CUSTOM_DURABILITY, new DoubleData(maxDurability));
                        mmo.getUpgradeTemplate().upgradeTo(mmo, data.getLevel() - 1);
                        return mmo.newBuilder().buildNBT().toItem();

                    } catch (JsonSyntaxException | IllegalStateException ignored) {
                        // Nothing
                    }
            }
        }

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
            int damage = (durability == maxDurability) ? 0 : Math.max(1, (int) ((1. - ((double) durability / maxDurability)) * nbtItem.getItem().getType().getMaxDurability()));
            nbtItem.addTag(new ItemTag("Damage", damage));
        }

        nbtItem.addTag(new ItemTag("MMOITEMS_DURABILITY", durability));

        // Apply the NBT tags
        ItemStack item = nbtItem.toItem();

        // Item lore update
        final String format = MythicLib.inst().parseColors(ItemStats.ITEM_DAMAGE.getGeneralStatFormat().replace("{max}", String.valueOf(maxDurability)));
        final String old = format.replace("{current}", String.valueOf(initialDurability));
        final String replaced = format.replace("{current}", String.valueOf(durability));
        return new LoreUpdate(item, old, replaced).updateLore();
    }
}
