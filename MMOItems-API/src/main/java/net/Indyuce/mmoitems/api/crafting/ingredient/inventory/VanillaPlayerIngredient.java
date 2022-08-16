package net.Indyuce.mmoitems.api.crafting.ingredient.inventory;

import io.lumine.mythic.lib.api.item.NBTItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class VanillaPlayerIngredient extends PlayerIngredient {
    @NotNull public ItemStack getSourceItem() { return sourceItem; }
    @NotNull final ItemStack sourceItem;

    private final Material material;
    @Nullable private final String displayName;

    public VanillaPlayerIngredient(NBTItem item) {
        super(item);

        // Restore item
        sourceItem = item.toItem();
        this.material = item.getItem().getType();

        ItemMeta meta = item.getItem().getItemMeta();
        if (meta != null) { this.displayName = meta.hasDisplayName() ? meta.getDisplayName() : null; } else { this.displayName = null; }
    }

    public Material getType() {
        return material;
    }

    @Nullable
    public String getDisplayName() {
        return displayName;
    }
}
