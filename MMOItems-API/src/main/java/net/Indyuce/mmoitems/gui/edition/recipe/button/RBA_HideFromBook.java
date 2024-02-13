package net.Indyuce.mmoitems.gui.edition.recipe.button;

import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import io.lumine.mythic.lib.api.util.ItemFactory;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.gui.edition.recipe.button.type.RBA_BooleanButton;
import net.Indyuce.mmoitems.gui.edition.recipe.gui.RecipeEditorGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.KnowledgeBookMeta;
import org.jetbrains.annotations.NotNull;

/**
 * Prevents the recipe from automatically being sent to players
 * to see freely in the recipe book.
 *
 * @author Gunging
 */
public class RBA_HideFromBook extends RBA_BooleanButton {

    /**
     * A button of an Edition Inventory. Nice!
     *
     * @param inv The edition inventory this is a button of
     */
    public RBA_HideFromBook(@NotNull RecipeEditorGUI inv) { super(inv); }

    public static final String BOOK_HIDDEN = "hidden";
    @NotNull @Override public String getBooleanConfigPath() { return BOOK_HIDDEN; }

    @Override public boolean runSecondary() {

        // Set value
        ItemStack book = new ItemStack(Material.KNOWLEDGE_BOOK);
        ItemMeta iMeta = book.getItemMeta();

        // Edit meta
        if (iMeta instanceof KnowledgeBookMeta) {

            // Add recipe
            ((KnowledgeBookMeta) iMeta).addRecipe(MMOItems.plugin.getRecipes().getRecipeKey(
                    getInv().getEdited().getType(),
                    getInv().getEdited().getId(),
                    getInv().getRecipeRegistry().getRecipeConfigPath(),
                    getInv().getRecipeName()));
        }

        // Set meta
        book.setItemMeta(iMeta);

        // Give it to the player
        getInv().getPlayer().getInventory().addItem(book);
        getInv().getPlayer().playSound(getInv().getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2);

        // Done
        return true; }

    @NotNull final ItemStack booleanButton = ItemFactory.of(Material.KNOWLEDGE_BOOK).name("\u00a7c隐藏工艺书(绿色的合成书)").lore(SilentNumbers.chop(
            "即使启用了制作书，玩家也不会自动解锁该配方"
            , 65, "\u00a77")).build();

    @NotNull @Override public ItemStack getBooleanButton() { return booleanButton; }

    @NotNull @Override public ItemStack getButton() {

        // Dictate the correct one
        String input = isEnabled() ? "\u00a7c不\u00a78, 它被隐藏了." : "\u00a7a是\u00a78, 它显示了.";

        // Copy and send
        return RecipeEditorGUI.addLore(getBooleanButton().clone(),
                SilentNumbers.toArrayList(
                "", "\u00a77在配方书中? " + input, "",
                        ChatColor.YELLOW + AltChar.listDash + " 右键生成配方解锁书",
                        ChatColor.YELLOW + AltChar.listDash + " 左键单击可切换此选项" ));
    }
}
