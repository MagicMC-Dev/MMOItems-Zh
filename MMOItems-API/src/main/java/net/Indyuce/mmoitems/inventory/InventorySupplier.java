package net.Indyuce.mmoitems.inventory;

import net.Indyuce.mmoitems.api.player.PlayerData;
import org.jetbrains.annotations.NotNull;

public interface InventorySupplier {

    /**
     * @param playerData Player data of online player
     * @return New inventory watcher
     */
    @NotNull
    public InventoryWatcher supply(@NotNull PlayerData playerData);
}
