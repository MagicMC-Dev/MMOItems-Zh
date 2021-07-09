package net.Indyuce.mmoitems.comp.mythicmobs.crafting;

import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.api.crafting.ingredient.inventory.PlayerIngredient;

/**
 * @deprecated Not implemented yet
 */
@Deprecated
public class MythicItemPlayerIngredient extends PlayerIngredient {
    private final String type, id;

    public MythicItemPlayerIngredient(NBTItem item) {
        super(item);

        type = item.getString("MYTHIC_TYPE").toLowerCase();

        // No idea what to use here
        id = null;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }
}
