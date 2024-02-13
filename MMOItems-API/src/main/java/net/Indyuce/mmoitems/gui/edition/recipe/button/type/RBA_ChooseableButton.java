package net.Indyuce.mmoitems.gui.edition.recipe.button.type;

import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import net.Indyuce.mmoitems.gui.edition.recipe.button.RecipeButtonAction;
import net.Indyuce.mmoitems.gui.edition.recipe.gui.RecipeEditorGUI;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * A button that cycles among a list of options, rather
 * than just a number or TRUE/FALSE.
 *
 * @author Gunging
 */
public abstract class RBA_ChooseableButton extends RecipeButtonAction {

    /**
     * A button of an Edition Inventory. Nice!
     *
     * @param inv The edition inventory this is a button of
     */
    public RBA_ChooseableButton(@NotNull RecipeEditorGUI inv) { super(inv); }

    /**
     * Cycles to the next value
     *
     * @return True
     */
    @Override public boolean runPrimary() {

        // Get current
        String current = getCurrentChooseableValue();

        // Included?
        int currentIndex = getChooseableList().indexOf(current);

        // Invalid value? Cancel and default
        if (currentIndex == -1) { return runSecondary(); }

        // Increase and Cap
        currentIndex++;
        if (currentIndex >= getChooseableList().size()) { currentIndex = 0; }

        // Get
        String next = getChooseableList().get(currentIndex);

        // Edits into persistent files
        getInv().getNameSection().set(getChooseableConfigPath(), next);
        clickSFX();

        // Save
        getInv().registerTemplateEdition();
        return true;
    }
    /**
     * Resets the list to the default value.
     *
     * @return True
     */
    @Override public boolean runSecondary() {
        // Clear the saved value
        getInv().getNameSection().set(getChooseableConfigPath(), null);
        clickSFX();

        // Save
        getInv().registerTemplateEdition();
        return true;
    }

    /**
     * @return The button ItemStack with its name and description. To
     *         it, all the chooseable values will be appended (as well
     *         as the definition of the current value chosen) when asked
     *         for in {@link #getButton()}
     */
    @NotNull public abstract ItemStack getChooseableButton();
    /**
     * @return Same as {@link #getChooseableButton()} but with
     *         the chooseable information appended to it.
     */
    @NotNull
    @Override
    public ItemStack getButton() {

        // Whats the current?
        String current = getCurrentChooseableValue();

        // Build lore to add: Current value and definition
        ArrayList<String> addedDefinitions = new ArrayList<>();
        addedDefinitions.add("\u00a77当前值:\u00a73 " + current);
        addedDefinitions.addAll(SilentNumbers.chop(getChooseableDefinition(current), 50, "  \u00a7b\u00a7o"));
        addedDefinitions.add("");
        addedDefinitions.add(ChatColor.YELLOW + AltChar.listDash + " 右键单击可返回默认值");
        addedDefinitions.add(ChatColor.YELLOW + AltChar.listDash + " 左键单击可循环选择选项:");
        for (String str : getChooseableList()) {

            // Is it the one?
            String pick = ChatColor.GOLD.toString();
            if (str.equals(current)) { pick = ChatColor.RED.toString() + ChatColor.BOLD;}

            addedDefinitions.add(pick + "  " + AltChar.smallListDash + " \u00a77" + str); }

        // Clone button and add the lore
        return RecipeEditorGUI.addLore(getChooseableButton().clone(), addedDefinitions);
    }

    /**
     * @return The path to save this value in the config
     */
    @NotNull public abstract String getChooseableConfigPath();

    /**
     * @return The list of different options the player may choose from.
     */
    @NotNull public abstract ArrayList<String> getChooseableList();
    /**
     * @return The value currently written onto the files.
     */
    @NotNull public String getCurrentChooseableValue() {

        // Get or default
        String ret = getInv().getNameSection().getString(getChooseableConfigPath());
        return ret != null ? ret : getDefaultValue();
    }
    /**
     * @return Of al the entries in {@link #getChooseableList()}, which
     *         is the default / initial one?
     */
    @NotNull public abstract String getDefaultValue();
    /**
     * @return Definition of what this choosing type does, for display in lore.
     *
     * @param ofChooseable Entry contained in the {@link #getChooseableList()} list.
     */
    @NotNull public abstract String getChooseableDefinition(@NotNull String ofChooseable);


    /**
     * This method doesnt run
     *
     * @param message Input from the user
     * @param info Additional objects, specific to each case, provided.
     *
     * @throws IllegalArgumentException Never
     */
    @Override public void secondaryProcessInput(@NotNull String message, Object... info) throws IllegalArgumentException { }
    /**
     * This method doesnt run
     *
     * @param message Input from the user
     * @param info Additional objects, specific to each case, provided.
     *
     * @throws IllegalArgumentException Never
     */
    @Override public void primaryProcessInput(@NotNull String message, Object... info) throws IllegalArgumentException { }
}
