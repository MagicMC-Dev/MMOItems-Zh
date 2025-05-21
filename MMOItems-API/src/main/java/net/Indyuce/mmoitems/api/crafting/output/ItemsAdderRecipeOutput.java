package net.Indyuce.mmoitems.api.crafting.output;

import dev.lone.itemsadder.api.CustomStack;
import io.lumine.mythic.lib.util.AdventureUtils;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class ItemsAdderRecipeOutput extends RecipeOutput {
    private final String id, display;

    public ItemsAdderRecipeOutput(ConfigObject config) {
        super(config);

        id = config.getString("id");
        display = config.contains("display") ? config.getString("display") : null;
    }

    @Override
    public ItemStack generateOutput(@NotNull RPGPlayer rpg) {
        return CustomStack.getInstance(id).getItemStack();
    }

    @Override
    public ItemStack getPreview() {
        ItemStack stack = CustomStack.getInstance(id).getItemStack();
        if (display != null) {
            ItemMeta meta = stack.getItemMeta();
            AdventureUtils.setDisplayName(meta, display);
            stack.setItemMeta(meta);
        }
        return stack;
    }
}
