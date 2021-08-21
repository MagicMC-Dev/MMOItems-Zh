package net.Indyuce.mmoitems.api.crafting.ingredient.inventory;

import com.google.gson.JsonParser;
import io.lumine.mythic.lib.api.item.NBTItem;

public class MMOItemPlayerIngredient extends PlayerIngredient {

    /**
     * No need to save as MMOItemTemplate or Type instances. Just need the string,
     * because if they don't exist the recipe ingredients won't load anyways.
     * And it's better for performance yes
     */
    private final String type, id;

    /**
     * Used to check if the level of the player's item matches
     * the level range given by the recipe ingredient.
     */
    private final int upgradeLevel;

    // TODO also add support for item level range? Quite easy to implement with the new PlayerIngredient interface.

    public MMOItemPlayerIngredient(NBTItem item) {
        super(item);

        this.type = item.getString("MMOITEMS_ITEM_TYPE");
        this.id = item.getString("MMOITEMS_ITEM_ID");

        String upgradeString = item.getString("MMOITEMS_UPGRADE");
        this.upgradeLevel = !upgradeString.isEmpty() ? new JsonParser().parse(upgradeString).getAsJsonObject().get("Level").getAsInt() : 0;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public int getUpgradeLevel() {
        return upgradeLevel;
    }
}
