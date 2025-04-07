package net.Indyuce.mmoitems.listener;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.version.Sounds;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.interaction.util.DurabilityItem;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DurabilityListener implements Listener {

    /**
     * Handles durability loss and item breaks for DAMAGEABLE items, VANILLA/CUSTOM durability
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void itemDamage(PlayerItemDamageEvent event) {
        final DurabilityItem item = DurabilityItem.from(event.getPlayer(), event.getItem());
        if (item == null) return;

        // Cap durability loss
        event.setDamage(capDurabilityLoss(event.getDamage()));

        item.onDurabilityDecrease(event.getDamage()); // Calculate item durability loss
        item.updateInInventory(event); // Update item
    }

    private static final List<DamageCause> IGNORED_CAUSES = Arrays.asList(DamageCause.DROWNING, DamageCause.SUICIDE, DamageCause.FALL, DamageCause.VOID,
            DamageCause.FIRE_TICK, DamageCause.SUFFOCATION, DamageCause.POISON, DamageCause.WITHER, DamageCause.STARVATION, DamageCause.MAGIC, DamageCause.KILL);
    private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    /**
     * Handles durability loss for NON-DAMAGEABLE items, CUSTOM durability.
     * <p>
     * Using priority HIGHEST to run after the attack
     * event which takes place at priority HIGH
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void playerDamage(EntityDamageEvent event) {
        if (event.getEntityType() != EntityType.PLAYER || IGNORED_CAUSES.contains(event.getCause()))
            return;

        Player player = (Player) event.getEntity();
        int damage = Math.max((int) event.getDamage() / 4, 1);
        for (EquipmentSlot slot : ARMOR_SLOTS)
            handleUndamageableItem(player, player.getInventory().getItem(slot), slot, damage);
    }

    private int capDurabilityLoss(int value) {
        final int cap = MMOItems.plugin.getLanguage().itemDurabilityLossCap;
        if (cap < 1) return value;
        return Math.min(cap, value);
    }

    private final Map<UUID, Long> lastAttack = new HashMap<>();
    private static final long ATTACK_TIMEOUT = 50;

    // Safeguard to avoid having the map blow up with time
    @EventHandler
    public void flushMap(PlayerQuitEvent event) {
        lastAttack.remove(event.getPlayer().getUniqueId());
    }

    /**
     * Handles durability loss for NON-DAMAGEABLE items, CUSTOM durability.
     * <p>
     * Using priority HIGHEST to run after the attack
     * event which takes place at priority HIGH
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void playerMeleeAttack(EntityDamageByEntityEvent event) {
        if (event.getDamage() == 0 || event.getCause() != DamageCause.ENTITY_ATTACK
                || !(event.getEntity() instanceof LivingEntity) || !UtilityMethods.isRealPlayer(event.getDamager()))
            return;

        // [NOT SUPER SAFE] No multiple durability loss within 1 tick
        @Nullable Long lastAttackLong = this.lastAttack.get(event.getDamager().getUniqueId());
        if (lastAttackLong != null && lastAttackLong + ATTACK_TIMEOUT > System.currentTimeMillis()) return;
        this.lastAttack.put(event.getDamager().getUniqueId(), System.currentTimeMillis());

        Player player = (Player) event.getDamager();
        ItemStack item = player.getInventory().getItemInMainHand();
        handleUndamageableItem(player, item, EquipmentSlot.HAND, 1);
    }

    /**
     * Handles mending exp for DAMAGEABLE items, CUSTOM durability.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void mendEvent(PlayerItemMendEvent event) {

        // Useless repair amount
        if (event.getRepairAmount() <= 0) return;

        DurabilityItem durItem = DurabilityItem.custom(null, event.getItem());
        if (durItem != null) {
            event.setCancelled(true); // Cancel event
            durItem.addDurability(event.getRepairAmount()); // Mend
            durItem.updateInInventory(); // Update inventory
        }
    }

    /**
     * This method is for all the items which have 0 max durability, which
     * are not breakable, hence the call to {@link Material#getMaxDurability()}
     */
    private void handleUndamageableItem(Player player, @Nullable ItemStack stack, EquipmentSlot slot, int damage) {
        if (UtilityMethods.isAir(stack) || stack.getType().getMaxDurability() > 0) return;

        final DurabilityItem item = DurabilityItem.custom(player, slot, stack);
        if (item == null) return;

        damage = capDurabilityLoss(damage); // Cap durability loss
        item.decreaseDurability(damage);

        if (item.updateInInventory().toItem() == null) {
            // Play break sound
            player.getWorld().playSound(player.getLocation(), Sounds.ENTITY_ITEM_BREAK, 1, 1);
        }
    }
}
