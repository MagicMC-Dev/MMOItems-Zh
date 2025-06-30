package net.Indyuce.mmoitems.inventory.provided;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.util.Lazy;
import io.lumine.mythic.lib.util.Pair;
import net.Indyuce.inventory.MMOInventory;
import net.Indyuce.inventory.api.event.InventoryUpdateEvent;
import net.Indyuce.inventory.inventory.Inventory;
import net.Indyuce.inventory.inventory.slot.CustomSlot;
import net.Indyuce.inventory.player.PlayerData;
import net.Indyuce.mmoitems.inventory.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static net.Indyuce.mmoitems.inventory.InventoryWatcher.optionalOf;

public class MMOInventorySupplier implements InventorySupplier, Listener {

    /*
     * TODO
     *
     * There's a known issue with this implementation. When MMOInventory is reloaded
     * so that it leaves an existing inventory non existent, items are still registered
     * until a server reload or the player logs out. This is because when previous
     * inventories are flushed, the MMOItems representations of items are not flushed
     */

    @NotNull
    @Override
    public InventoryWatcher supply(@NotNull InventoryResolver resolver) {
        return new Watcher(resolver);
    }

    private static class Watcher extends InventoryWatcher {
        private final Player player;

        private final Map<Pair<Integer, Integer>, EquippedItem> equipped = new HashMap<>();
        private final Lazy<PlayerData> playerData;

        private Watcher(InventoryResolver resolver) {
            this.player = resolver.getPlayerData().getPlayer();
            this.playerData = Lazy.persistent(() -> MMOInventory.plugin.getDataManager().get(player));
        }

        @Nullable
        public ItemUpdate watchAccessory(Inventory inventory, CustomSlot slot, @NotNull Optional<ItemStack> newItem) {
            ItemStack stack = newItem.orElse(playerData.get().get(inventory).getItem(slot));
            final Pair<Integer, Integer> uniqueMapKey = Pair.of(inventory.getIntegerId(), slot.getIndex());
            ItemUpdate update = checkForUpdate(stack, equipped.get(uniqueMapKey), EquipmentSlot.ACCESSORY, slot.getIndex(), inventory.getIntegerId());
            if (update != null) equipped.put(uniqueMapKey, update.getNew());
            return update;
        }

        @Override
        public void watchAll(@NotNull Consumer<ItemUpdate> callback) {
            for (Inventory inv : MMOInventory.plugin.getInventoryManager().getAll())
                for (CustomSlot slot : inv.getSlots())
                    if (slot.getType().isCustom())
                        callbackIfNotNull(watchAccessory(inv, slot, Optional.empty()), callback);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void click(InventoryUpdateEvent event) {
        ItemStack equipped = event.getNewItem();
        net.Indyuce.mmoitems.api.player.PlayerData.get(event.getPlayerData().getPlayer()).getInventory().watch(Watcher.class, watcher -> watcher.watchAccessory(event.getInventory(), event.getSlot(), optionalOf(equipped)));
    }
}
