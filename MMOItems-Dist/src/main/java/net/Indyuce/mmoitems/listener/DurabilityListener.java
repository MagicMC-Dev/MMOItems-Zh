package net.Indyuce.mmoitems.listener;

import net.Indyuce.mmoitems.api.interaction.util.DurabilityItem;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class DurabilityListener implements Listener {

    /**
     * Handles durability for ANY damageable items.
     */
    @EventHandler(ignoreCancelled = true)
    public void itemDamage(PlayerItemDamageEvent event) {
        final DurabilityItem item = new DurabilityItem(event.getPlayer(), event.getItem());
        if (!item.isValid())
            return;

        // Calculate item durability loss
        item.decreaseDurability(event.getDamage());

        /*
         * If the item is broken and if it is meant to be lost when broken,
         * do NOT cancel the event and make sure the item is destroyed
         */
        final ItemStack newVersion = item.toItem();
        if (newVersion == null) {
            event.setDamage(999);
            return;
        }

        event.setCancelled(true);
        event.getItem().setItemMeta(newVersion.getItemMeta());
    }

    private static final List<DamageCause> IGNORED_CAUSES = Arrays.asList(DamageCause.DROWNING, DamageCause.SUICIDE, DamageCause.FALL, DamageCause.VOID,
            DamageCause.FIRE_TICK, DamageCause.SUFFOCATION, DamageCause.POISON, DamageCause.WITHER, DamageCause.STARVATION, DamageCause.MAGIC);
    private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    @EventHandler(ignoreCancelled = true)
    public void playerDamage(EntityDamageEvent event) {
        if (event.getEntityType() != EntityType.PLAYER || IGNORED_CAUSES.contains(event.getCause()))
            return;

        Player player = (Player) event.getEntity();
        int damage = Math.max((int) event.getDamage() / 4, 1);
        for (EquipmentSlot slot : ARMOR_SLOTS)
            if (hasItem(player, slot))
                handleUndamageableItem(player.getInventory().getItem(slot), player, slot, damage);
    }

    @EventHandler(ignoreCancelled = true)
    public void playerMeleeAttack(EntityDamageByEntityEvent event) {
        if (event.getDamage() == 0 || event.getCause() != DamageCause.ENTITY_ATTACK || !(event.getEntity() instanceof LivingEntity)
                || !(event.getDamager() instanceof Player) || event.getEntity().hasMetadata("NPC") || event.getDamager().hasMetadata("NPC"))
            return;
        Player player = (Player) event.getDamager();
        ItemStack item = player.getInventory().getItemInMainHand();

        handleUndamageableItem(item, player, EquipmentSlot.HAND, 1);
    }

    @EventHandler(ignoreCancelled = true)
    public void playerBowAttack(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;
        Player player = (Player) event.getEntity();
        ItemStack item = event.getBow();

        handleUndamageableItem(item, player, EquipmentSlot.HAND, 1);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void mendEvent(PlayerItemMendEvent event) {
        DurabilityItem durItem = new DurabilityItem(event.getPlayer(), event.getItem());
        if (durItem.isValid()) {
            event.getItem().setItemMeta(durItem.addDurability(event.getRepairAmount()).toItem().getItemMeta());
            event.setCancelled(true);
        }
    }

    /**
     * This method is for all the items which have 0 max durability i.e
     * which are not breakable hence the {@link Material#getMaxDurability()}
     */
    private void handleUndamageableItem(ItemStack stack, Player player, EquipmentSlot slot, int damage) {
        if (stack.getType().getMaxDurability() != 0)
            return;

        final DurabilityItem item = new DurabilityItem(player, stack);
        if (!item.isValid())
            return;

        item.decreaseDurability(damage);

        ItemStack newVersion = item.toItem();
        if (newVersion == null) {
            player.getInventory().setItem(slot, null);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            return;
        }

        player.getInventory().getItem(slot).setItemMeta(newVersion.getItemMeta());
    }

    private boolean hasItem(Player player, EquipmentSlot slot) {
        return player.getInventory().getItem(slot) != null && player.getInventory().getItem(slot).getType() != Material.AIR;
    }
}
