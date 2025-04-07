package net.Indyuce.mmoitems.api.crafting.ingredient.inventory;

import dev.lone.itemsadder.api.CustomStack;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.util.lang3.Validate;

public class ItemsAdderPlayerIngredient extends PlayerIngredient {
    private final String id;

    public ItemsAdderPlayerIngredient(NBTItem item) {
        super(item);

        CustomStack stack = CustomStack.byItemStack(item.getItem());
        Validate.notNull(stack, "Not a custom item");
        id = stack.getId();
    }

    public String getId() {
        return id;
    }
}
