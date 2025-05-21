package net.Indyuce.mmoitems.inventory;

import org.jetbrains.annotations.NotNull;

public interface InventorySupplier {

    /**
     * @param resolver Player to watch inventory of
     * @return New inventory watcher
     */
    @NotNull
    public InventoryWatcher supply(@NotNull InventoryResolver resolver);
}
