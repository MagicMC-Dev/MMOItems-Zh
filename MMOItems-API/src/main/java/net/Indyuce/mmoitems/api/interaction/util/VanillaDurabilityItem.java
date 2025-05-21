package net.Indyuce.mmoitems.api.interaction.util;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.util.lang3.Validate;
import net.Indyuce.mmoitems.ItemStats;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VanillaDurabilityItem extends DurabilityItem {
    private final int maxDamage, initialDamage;
    private final ItemMeta meta;

    private int damage;

    protected VanillaDurabilityItem(@Nullable Player player, @NotNull NBTItem nbtItem, @Nullable EquipmentSlot slot) {
        super(player, nbtItem, slot);

        meta = item.getItemMeta();
        Validate.isTrue(meta instanceof Damageable, "Item is not damageable");
        maxDamage = retrieveMaxVanillaDurability(item, meta);
        Validate.isTrue(maxDamage > 0, "No max damage");
        initialDamage = ((Damageable) meta).getDamage();
        damage = initialDamage;
        Validate.isTrue(!meta.isUnbreakable(), "Item is unbreakable");
    }

    @NotNull
    @Override
    protected ItemStack applyChanges() {

        if (damage == initialDamage) return item;

        // #setDamage throws an error if damage negative or too high
        ((Damageable) meta).setDamage(Math.max(0, Math.min(damage, maxDamage)));
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public void updateInInventory(@NotNull PlayerItemDamageEvent event) {

        // Just skip if the item is not broken. Useless performance hit.
        if (!isBroken()) return;

        super.updateInInventory(event);
    }

    @Override
    public boolean isBroken() {
        return damage >= maxDamage;
    }

    @Deprecated
    public boolean wouldBreak(int extraDamage) {
        return damage + extraDamage >= maxDamage;
    }

    @Override
    public boolean isLostWhenBroken() {
        // [BACKWARDS COMPATIBILITY] The opposite of custom durability, to mimic vanilla behaviour.
        return !nbtItem.getBoolean(ItemStats.WILL_BREAK.getNBTPath());
    }

    @Override
    public int getDurability() {
        return maxDamage - damage;
    }

    @Override
    public int getMaxDurability() {
        return maxDamage;
    }

    @Override
    public void onDurabilityAdd(int gain) {
        // TODO call event
        damage -= gain;
    }

    @Override
    public void onDurabilityDecrease(int loss) {
        damage += loss;
    }
}
