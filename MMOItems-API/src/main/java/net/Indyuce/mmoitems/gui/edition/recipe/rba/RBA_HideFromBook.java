package net.Indyuce.mmoitems.gui.edition.recipe.rba;

import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import io.lumine.mythic.lib.api.util.ItemFactory;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.gui.edition.recipe.rba.type.RBA_BooleanButton;
import net.Indyuce.mmoitems.gui.edition.recipe.gui.RecipeMakerGUI;
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
    public RBA_HideFromBook(@NotNull RecipeMakerGUI inv) { super(inv); }

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

    @NotNull final ItemStack booleanButton = RecipeMakerGUI.addLore(ItemFactory.of(Material.KNOWLEDGE_BOOK).name("\u00a7cHide from Crafting Book").lore(SilentNumbers.chop(
            "Even if the crafting book is enabled, this recipe wont be automatically unlocked by players."
            , 65, "\u00a77")).build(), SilentNumbers.toArrayList(""));

    @NotNull @Override public ItemStack getBooleanButton() { return booleanButton; }

    @NotNull @Override public ItemStack getButton() {

        // Dictate the correct one
        String input = isEnabled() ? "\u00a7cNO\u00a78, it's hidden." : "\u00a7aYES\u00a78, it's shown.";

        // Copy and send
        return RecipeMakerGUI.addLore(getBooleanButton().clone(),
                SilentNumbers.toArrayList(
                "", "\u00a77Currently in Book? " + input, "",
                        ChatColor.YELLOW + AltChar.listDash + " Right click to generate recipe unlock book.",
                        ChatColor.YELLOW + AltChar.listDash + " Left click to toggle this option." ));
    }
}
