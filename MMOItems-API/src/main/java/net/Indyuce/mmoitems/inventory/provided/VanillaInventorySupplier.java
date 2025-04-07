package net.Indyuce.mmoitems.inventory.provided;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.inventory.EquippedItem;
import net.Indyuce.mmoitems.inventory.InventorySupplier;
import net.Indyuce.mmoitems.inventory.InventoryWatcher;
import net.Indyuce.mmoitems.inventory.ItemUpdate;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Consumer;

import static net.Indyuce.mmoitems.inventory.InventoryWatcher.*;

public class VanillaInventorySupplier implements InventorySupplier, Listener {

    @NotNull
    @Override
    public InventoryWatcher supply(@NotNull PlayerData playerData) {
        return new Watcher(playerData);
    }

    private static class Watcher implements InventoryWatcher {
        private final Player player;

        private EquippedItem helmet, chestplate, leggings, boots, mainhand, offhand;

        public Watcher(PlayerData playerData) {
            this.player = playerData.getPlayer();
        }

        @Override
        public void watchAll(@NotNull Consumer<ItemUpdate> callback) {
            callbackIfNotNull(watchSingle(EquipmentSlot.HEAD), callback);
            callbackIfNotNull(watchSingle(EquipmentSlot.CHEST), callback);
            callbackIfNotNull(watchSingle(EquipmentSlot.LEGS), callback);
            callbackIfNotNull(watchSingle(EquipmentSlot.FEET), callback);
            callbackIfNotNull(watchSingle(EquipmentSlot.MAIN_HAND), callback);
            callbackIfNotNull(watchSingle(EquipmentSlot.OFF_HAND), callback);
        }

        @Override
        public ItemUpdate watchSingle(@NotNull EquipmentSlot slot, int ignored, @NotNull Optional<ItemStack> newItem) {
            switch (slot) {
                case HEAD: {
                    ItemStack stack = newItem.orElse(player.getEquipment().getHelmet());
                    ItemUpdate update = checkForUpdate(stack, helmet, EquipmentSlot.HEAD);
                    if (update != null) helmet = update.getNew();
                    return update;
                }
                case CHEST: {
                    ItemStack stack = newItem.orElse(player.getEquipment().getChestplate());
                    ItemUpdate update = checkForUpdate(stack, chestplate, EquipmentSlot.CHEST);
                    if (update != null) chestplate = update.getNew();
                    return update;
                }
                case LEGS: {
                    ItemStack stack = newItem.orElse(player.getEquipment().getLeggings());
                    ItemUpdate update = checkForUpdate(stack, leggings, EquipmentSlot.LEGS);
                    if (update != null) leggings = update.getNew();
                    return update;
                }
                case FEET: {
                    ItemStack stack = newItem.orElse(player.getEquipment().getBoots());
                    ItemUpdate update = checkForUpdate(stack, boots, EquipmentSlot.FEET);
                    if (update != null) boots = update.getNew();
                    return update;
                }
                case MAIN_HAND: {
                    ItemStack stack = newItem.orElse(player.getEquipment().getItemInMainHand());
                    ItemUpdate update = checkForUpdate(stack, mainhand, EquipmentSlot.MAIN_HAND);
                    if (update != null) mainhand = update.getNew();
                    return update;
                }
                case OFF_HAND: {
                    ItemStack stack = newItem.orElse(player.getEquipment().getItemInOffHand());
                    ItemUpdate update = checkForUpdate(stack, offhand, EquipmentSlot.OFF_HAND);
                    if (update != null) offhand = update.getNew();
                    return update;
                }

                // Not my job
                default:
                    return null;
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPressF(PlayerSwapHandItemsEvent event) {
        PlayerData playerData = PlayerData.get(event.getPlayer());
        EntityEquipment equipment = event.getPlayer().getEquipment();

        // Items are not swapped yet
        playerData.getMMOPlayerData().getStatMap().bufferUpdates(() -> {
            playerData.getInventory().watchSingle(EquipmentSlot.MAIN_HAND, optionalOf(equipment.getItemInOffHand()));
            playerData.getInventory().watchSingle(EquipmentSlot.OFF_HAND, optionalOf(equipment.getItemInMainHand()));
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onCursorChange(PlayerItemHeldEvent event) {
        // New item is hotbar item with index `event.getNewSlot()`
        ItemStack itemHeld = event.getPlayer().getInventory().getItem(event.getNewSlot());
        PlayerData.get(event.getPlayer()).getInventory().watchSingle(EquipmentSlot.MAIN_HAND, optionalOf(itemHeld));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onDropItem(PlayerDropItemEvent event) {
        /*
         * Cannot really make a difference between an item drop
         * when using Q, and a drop due to an inventory click. inventory
         * clicks do not require a vanilla watch
         */
        //Bukkit.broadcastMessage("item dropped " + event.getItemDrop().getItemStack().getType());
        // TODO test if this works :(
        PlayerData.get(event.getPlayer()).getInventory().watchSingle(EquipmentSlot.MAIN_HAND);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onCloseInventory(InventoryCloseEvent event) {

        //Bukkit.broadcastMessage("Inventory closed");

        /*
         * This is to avoid players moving an item that was on their hand slot
         * TODO use inventoryclick and check for slot instead.
         */
        if (event.getPlayer() instanceof Player) {
            try {
                // Sometimes the event is called after the player logs off?
                PlayerData playerData = PlayerData.get((Player) event.getPlayer());
                playerData.getInventory().watchSingle(EquipmentSlot.MAIN_HAND);
            } catch (Exception exception) {
                // Ignore for now
            }
        }
    }
}
