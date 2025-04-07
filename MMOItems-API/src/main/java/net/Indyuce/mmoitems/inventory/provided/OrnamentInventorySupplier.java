package net.Indyuce.mmoitems.inventory.provided;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.inventory.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

public class OrnamentInventorySupplier implements InventorySupplier, Listener {
    public OrnamentInventorySupplier() {
        InventoryResolver.ENABLE_ORNAMENTS = true;
    }

    @NotNull
    @Override
    public InventoryWatcher supply(@NotNull PlayerData playerData) {
        return new Watcher(playerData);
    }

    private static class Watcher implements InventoryWatcher {
        private final Player player;

        private final EquippedItem[] matrixEquipped;

        public Watcher(PlayerData playerData) {
            this.player = playerData.getPlayer();
            int inventorySize = playerData.getPlayer().getInventory().getContents().length;
            this.matrixEquipped = new EquippedItem[inventorySize];
        }

        @Override
        public void watchAll(@NotNull Consumer<ItemUpdate> callback) {
            for (int i = 0; i < matrixEquipped.length; i++)
                InventoryWatcher.callbackIfNotNull(watchSingle(EquipmentSlot.INVENTORY, i), callback);
        }

        @Nullable
        @Override
        public ItemUpdate watchSingle(@NotNull EquipmentSlot slot, int index, @NotNull Optional<ItemStack> newItem) {

            // Not my job
            if (slot != EquipmentSlot.INVENTORY) return null;

            ItemStack stack = newItem.orElse(player.getInventory().getContents()[index]);
            ItemUpdate update = InventoryWatcher.checkForUpdate(stack, matrixEquipped[index], EquipmentSlot.INVENTORY, index);
            if (update != null) matrixEquipped[index] = update.getNew();
            return update;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void updateOnItemPickup(EntityPickupItemEvent event) {
        if (event.getEntityType() != EntityType.PLAYER) return;

        // TODO can be further optimized
        final Type type = Type.get(NBTItem.get(event.getItem().getItemStack()));
        if (type != null && type.corresponds(Type.ORNAMENT))
            PlayerData.get((Player) event.getEntity()).resolveInventory();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void updateOnItemDrop(PlayerDropItemEvent event) {
        // TODO can be further optimized
        final Type type = Type.get(NBTItem.get(event.getItemDrop().getItemStack()));
        if (type != null && type.corresponds(Type.ORNAMENT))
            PlayerData.get(event.getPlayer()).resolveInventory();
    }
}

