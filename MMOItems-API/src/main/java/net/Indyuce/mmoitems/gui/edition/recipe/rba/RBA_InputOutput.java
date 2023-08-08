package net.Indyuce.mmoitems.gui.edition.recipe.rba;

import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import io.lumine.mythic.lib.api.util.ItemFactory;
import net.Indyuce.mmoitems.gui.edition.recipe.gui.RecipeMakerGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Button to switch between Input and Output modes of the station.
 *
 * @author Gunging
 */
public class RBA_InputOutput extends RecipeButtonAction {

    boolean showingInput;

    /**
     * A button of an Edition Inventory. Nice!
     *
     * @param inv The edition inventory this is a button of
     */
    public RBA_InputOutput(@NotNull RecipeMakerGUI inv) {
        super(inv);

        // By default, input is shown.
        showingInput = true;
    }

    @Override
    public boolean runPrimary() {
        getInv().switchInput();
        getInv().refreshInventory();
        clickSFX();
        return true;
    }

    /**
     * This method never runs.
     *
     * @param message Input from the user
     * @param info Additional objects, specific to each case, provided.
     *
     * @throws IllegalArgumentException Never
     */
    @Override public void primaryProcessInput(@NotNull String message, Object... info) throws IllegalArgumentException { }

    /**
     * Nothing happens
     */
    @Override public boolean runSecondary() { return false; }


    /**
     * This method never runs.
     *
     * @param message Input from the user
     * @param info Additional objects, specific to each case, provided.
     *
     * @throws IllegalArgumentException Never
     */
    @Override public void secondaryProcessInput(@NotNull String message, Object... info) throws IllegalArgumentException { }

    @NotNull final ItemStack button = RecipeMakerGUI.addLore(ItemFactory.of(Material.CRAFTING_TABLE).name("\u00a7c切换到输出模式").lore(SilentNumbers.chop(
            "INPUT 是配方的材料, 但是 (就像制作蛋糕时的牛奶桶一样) 这些材料可能不会完全消耗在这种情况下, 请使用 OUTPUT 模式来指定材料将变成什么"
            , 63, "\u00a77")).build(), SilentNumbers.toArrayList(""));

    @NotNull
    @Override
    public ItemStack getButton() {

        // Dictate the correct one
        String input = getInv().isShowingInput() ? "\u00a76输入" : "\u00a73输出";

        // Copy and send
        return RecipeMakerGUI.addLore(button.clone(), SilentNumbers.toArrayList("\u00a77目前正在展示: " + input, "",
                ChatColor.YELLOW + AltChar.listDash + "► 左键单击切换模式" ));
    }
}
