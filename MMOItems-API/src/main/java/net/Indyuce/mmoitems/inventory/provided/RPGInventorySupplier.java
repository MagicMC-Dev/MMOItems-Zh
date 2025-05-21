package net.Indyuce.mmoitems.inventory.provided;

import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.inventory.InventoryResolver;
import net.Indyuce.mmoitems.inventory.InventorySupplier;
import net.Indyuce.mmoitems.inventory.InventoryWatcher;
import net.Indyuce.mmoitems.inventory.ItemUpdate;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class RPGInventorySupplier implements InventorySupplier {

    @NotNull
    @Override
    public InventoryWatcher supply(@NotNull InventoryResolver resolver) {
        return new Watcher(resolver);
    }

    private static class Watcher extends InventoryWatcher {
        private final PlayerData playerData;

        private final Map<ItemStack, Integer> itemHashes = new HashMap<>();

        public Watcher(InventoryResolver resolver) {
            this.playerData = resolver.getPlayerData();
        }

        @Override
        public void watchAll(@NotNull Consumer<ItemUpdate> callback) {




            // TODO
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