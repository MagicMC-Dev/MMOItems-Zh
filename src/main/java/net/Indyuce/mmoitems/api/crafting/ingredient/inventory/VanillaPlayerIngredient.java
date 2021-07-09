package net.Indyuce.mmoitems.api.crafting.ingredient.inventory;

import io.lumine.mythic.lib.api.item.NBTItem;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;

public class VanillaPlayerIngredient extends PlayerIngredient {
    private final Material material;
    @Nullable
    private final String displayName;

    public VanillaPlayerIngredient(NBTItem item) {
        super(item);

        this.material = item.getItem().getType();

        ItemMeta meta = item.getItem().getItemMeta();
        this.displayName = meta.hasDisplayName() ? meta.getDisplayName() : null;
    }

    public Material getType() {
        return material;
    }

    @Nullable
    public String getDisplayName() {
        return displayName;
    }
}
