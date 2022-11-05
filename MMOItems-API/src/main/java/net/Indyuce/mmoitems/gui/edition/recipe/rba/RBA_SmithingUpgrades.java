package net.Indyuce.mmoitems.gui.edition.recipe.rba;

import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import io.lumine.mythic.lib.api.util.ItemFactory;
import net.Indyuce.mmoitems.api.crafting.recipe.SmithingCombinationType;
import net.Indyuce.mmoitems.gui.edition.recipe.rba.type.RBA_ChooseableButton;
import net.Indyuce.mmoitems.gui.edition.recipe.gui.RecipeMakerGUI;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Which behaviour do Upgrades follow when the player smiths items?
 *
 * @author Gunging
 */
public class RBA_SmithingUpgrades extends RBA_ChooseableButton {
    /**
     * A button of an Edition Inventory. Nice!
     *
     * @param inv The edition inventory this is a button of
     */
    public RBA_SmithingUpgrades(@NotNull RecipeMakerGUI inv) { super(inv); }

    @NotNull final ItemStack chooseableButton = ItemFactory.of(Material.ANVIL).name("\u00a7aUpgrades Transfer").lore(SilentNumbers.chop(
            "What will happen to the upgrades of the ingredients? Will upgraded ingredients produce an upgraded output item?"
            , 65, "\u00a77")).build();

    @NotNull @Override public ItemStack getChooseableButton() { return chooseableButton; }

    public static final String SMITH_UPGRADES = "upgrades";
    @NotNull @Override public String getChooseableConfigPath() { return SMITH_UPGRADES; }
    @NotNull @Override public ArrayList<String> getChooseableList() { return getSmithingList(); }
    @NotNull @Override public String getDefaultValue() { return SmithingCombinationType.MAXIMUM.toString(); }
    @NotNull @Override public String getChooseableDefinition(@NotNull String ofChooseable) {
        SmithingCombinationType sct = SmithingCombinationType.MAXIMUM;
        try { sct = SmithingCombinationType.valueOf(getCurrentChooseableValue()); } catch (IllegalArgumentException ignored) {}

        switch (sct) {
            case EVEN:
                return "Will take the average of the upgrade levels of the combined items.";
            case NONE:
                return "Will ignore the upgrade levels of any ingredients.";
            case MAXIMUM:
                return "Output will have the upgrade level of the most upgraded ingredient.";
            case MINIMUM:
                return "Output will have the upgrade level of the least-upgraded upgradeable ingredient.";
            case ADDITIVE:
                return "The upgrade levels of the ingredients will be added, and the result will be the crafted item's level.";

            default: return "Unknown behaviour. Add description in net.Indyuce.mmoitems.gui.edition.recipe.rba.RBA_SmithingUpgrades";
        }
    }

    static ArrayList<String> smithingList;
    /**
     * @return The allowed values of the smithing combination type list
     */
    @NotNull static ArrayList<String> getSmithingList() {
        if (smithingList != null) { return smithingList; }
        smithingList = new ArrayList<>();
        for (SmithingCombinationType sct : SmithingCombinationType.values()) { smithingList.add(sct.toString()); }
        return smithingList; }
}
