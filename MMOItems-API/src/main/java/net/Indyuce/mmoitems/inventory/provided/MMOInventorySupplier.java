package net.Indyuce.mmoitems.inventory.provided;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.util.Lazy;
import net.Indyuce.inventory.MMOInventory;
import net.Indyuce.inventory.api.event.ItemEquipEvent;
import net.Indyuce.inventory.inventory.InventoryHandler;
import net.Indyuce.inventory.slot.CustomSlot;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.inventory.EquippedItem;
import net.Indyuce.mmoitems.inventory.InventorySupplier;
import net.Indyuce.mmoitems.inventory.InventoryWatcher;
import net.Indyuce.mmoitems.inventory.ItemUpdate;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import static net.Indyuce.mmoitems.inventory.InventoryWatcher.optionalOf;

public class MMOInventorySupplier implements InventorySupplier, Listener {
    @NotNull
    @Override
    public InventoryWatcher supply(@NotNull PlayerData playerData) {
        return new Watcher(playerData);
    }

    private static class Watcher implements InventoryWatcher {
        private final Player player;

        private final Map<Integer, EquippedItem> equipped = new HashMap<>();
        private final Lazy<InventoryHandler> handler;

        private Watcher(PlayerData playerData) {
            this.player = playerData.getPlayer();
            this.handler = Lazy.persistent(() -> MMOInventory.plugin.getDataManager().get(player));
        }

        @Nullable
        @Override
        public ItemUpdate watchSingle(@NotNull EquipmentSlot slot, int index, @NotNull Optional<ItemStack> newItem) {
            if (slot != EquipmentSlot.ACCESSORY) return null;

            // Find new item
            ItemStack stack = newItem.orElse(handler.get().getItem(Objects.requireNonNull(MMOInventory.plugin.getSlotManager().get(index), "No slot with index " + index)));
            ItemUpdate update = InventoryWatcher.checkForUpdate(stack, equipped.get(index), slot, index);
            if (update != null) equipped.put(index, update.getNew());
            return update;
        }

        @Override
        public void watchAll(@NotNull Consumer<ItemUpdate> callback) {
            for (CustomSlot slot : MMOInventory.plugin.getSlotManager().getCustomSlots())
                InventoryWatcher.callbackIfNotNull(watchSingle(EquipmentSlot.ACCESSORY, slot.getIndex()), callback);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void click(ItemEquipEvent event) {
        ItemStack equipped = event.getItem();
        final int accessoryIndex = event.getSlot().getIndex();
        PlayerData.get(event.getPlayer()).getInventory().watchSingle(EquipmentSlot.ACCESSORY, accessoryIndex, optionalOf(equipped));
    }
}
