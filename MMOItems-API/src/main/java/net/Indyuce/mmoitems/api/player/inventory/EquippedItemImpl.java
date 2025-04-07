package net.Indyuce.mmoitems.api.player.inventory;

import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

@Deprecated
public class EquippedItemImpl extends EquippedItem {
    private final net.Indyuce.mmoitems.inventory.EquippedItem wrapped;

    @Deprecated
    public EquippedItemImpl(net.Indyuce.mmoitems.inventory.EquippedItem wrapped) {
        super(wrapped);

        this.wrapped = wrapped;
    }

    @Deprecated
    public void setItem(@Nullable ItemStack item) {
        wrapped.setItem(item);
    }
}