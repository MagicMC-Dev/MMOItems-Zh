package net.Indyuce.mmoitems.api.crafting.ingredient.inventory;

import com.nexomc.nexo.api.NexoItems;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.util.lang3.Validate;

public class NexoPlayerIngredient extends PlayerIngredient {
    private final String id;

    public NexoPlayerIngredient(NBTItem item) {
        super(item);

        String id = NexoItems.idFromItem(item.getItem());
        Validate.notNull(id, "Not a Nexo item");
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
