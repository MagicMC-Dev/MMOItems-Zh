package net.Indyuce.mmoitems.inventory.provided;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.inventory.InventorySupplier;
import net.Indyuce.mmoitems.inventory.InventoryWatcher;
import net.Indyuce.mmoitems.inventory.ItemUpdate;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Consumer;

public class RPGInventorySupplier implements InventorySupplier {

    @NotNull
    @Override
    public InventoryWatcher supply(@NotNull PlayerData playerData) {
        return new Watcher(playerData);
    }

    private static class Watcher implements InventoryWatcher {
        private final PlayerData playerData;

        public Watcher(PlayerData playerData) {
            this.playerData = playerData;
        }

        @Override
        public void watchAll(@NotNull Consumer<ItemUpdate> callback) {

        }

        @Override
        public ItemUpdate watchSingle(@NotNull EquipmentSlot slot, int index, @NotNull Optional<ItemStack> newItem) {
            throw new RuntimeException("TODO");
        }
    }
}

/*

@Deprecated
public class RPGInventoryHook implements PlayerInventory, Listener {

    @Override
    public List<EquippedItem> getInventory(Player player) {
        List<EquippedItem> list = new ArrayList<>();

        for (ItemStack passive : InventoryAPI.getPassiveItems(player))
            if (passive != null)
                list.add(new LegacyEquippedItem(passive));

        return list;
    }

    @EventHandler
    public void a(InventoryCloseEvent event) {
        if (InventoryAPI.isRPGInventory(event.getInventory()))
            PlayerData.get((Player) event.getPlayer()).updateInventory();
    }

    public class LegacyEquippedItem extends EquippedItem {
        public LegacyEquippedItem(ItemStack item) {
            super(item, EquipmentSlot.ACCESSORY);
        }

        @Override
        public void setItem(@Nullable ItemStack item) {
            final ItemStack ref = getNBT().getItem();
            ref.setType(item.getType());
            ref.setAmount(item.getAmount());
            ref.setItemMeta(ref.getItemMeta());
        }
    }
}

 */