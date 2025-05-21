package net.Indyuce.mmoitems.api.crafting.ingredient;

import dev.lone.itemsadder.api.CustomStack;
import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.util.lang3.Validate;
import net.Indyuce.mmoitems.api.crafting.ingredient.inventory.ItemsAdderPlayerIngredient;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.util.MMOUtils;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemsAdderIngredient extends Ingredient<ItemsAdderPlayerIngredient> {
    private final String id;
    private final String display;

    public ItemsAdderIngredient(MMOLineConfig config) {
        super("itemsadder", config);

        config.validate("id");
        id = config.getString("id");

        // Find the display name of the item
        display = config.contains("display") ? config.getString("display") : findName();
    }

    @Override
    public String formatDisplay(String s) {
        return s.replace("#item#", display).replace("#amount#", String.valueOf(getAmount()));
    }

    @Override
    public boolean matches(ItemsAdderPlayerIngredient playerIngredient) {
        return playerIngredient.getId().equals(id);
    }

    @NotNull
    @Override
    public ItemStack generateItemStack(@NotNull RPGPlayer player, boolean forDisplay) {
        CustomStack item = CustomStack.getInstance(id);
        Validate.notNull(item, String.format("Could not find item with ID '%s'", id));

        ItemStack generated = item.getItemStack();
        generated.setAmount(getAmount());
        return generated;
    }

    @Override
    public String toString() {
        return "ItemsAdderIngredient{" +
                "id='" + id + '\'' +
                '}';
    }

    private String findName() {
        CustomStack tryGenerate = CustomStack.getInstance(id);
        return MMOUtils.getDisplayName(tryGenerate.getItemStack());
    }
}
