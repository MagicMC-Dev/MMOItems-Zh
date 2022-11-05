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
 * Which behaviour do Enchantments follow when the player smiths items?
 *
 * @author Gunging
 */
public class RBA_SmithingEnchantments extends RBA_ChooseableButton {

    /**
     * A button of an Edition Inventory. Nice!
     *
     * @param inv The edition inventory this is a button of
     */
    public RBA_SmithingEnchantments(@NotNull RecipeMakerGUI inv) { super(inv); }

    @NotNull final ItemStack chooseableButton = ItemFactory.of(Material.ENCHANTING_TABLE).name("\u00a7aEnchantment Transfer").lore(SilentNumbers.chop(
            "What will happen to the enchantments of the ingredients? Will enchanted ingredients produce an enchanted output item?"
            , 65, "\u00a77")).build();

    @NotNull @Override public ItemStack getChooseableButton() { return chooseableButton; }

    public static final String SMITH_ENCHANTS = "enchantments";
    @NotNull @Override public String getChooseableConfigPath() { return SMITH_ENCHANTS; }
    @NotNull @Override public ArrayList<String> getChooseableList() { return RBA_SmithingUpgrades.getSmithingList(); }
    @NotNull @Override public String getDefaultValue() { return SmithingCombinationType.MAXIMUM.toString(); }
    @NotNull @Override public String getChooseableDefinition(@NotNull String ofChooseable) {
        SmithingCombinationType sct = SmithingCombinationType.MAXIMUM;
        try { sct = SmithingCombinationType.valueOf(getCurrentChooseableValue()); } catch (IllegalArgumentException ignored) {}

        switch (sct) {
            case EVEN:
                return "For each enchantment, will take the average of that enchantment's level across the ingredients.";
            case NONE:
                return "Will ignore the enchantments of any ingredients.";
            case MAXIMUM:
                return "Output will have the best enchantment from each ingredient";
            case MINIMUM:
                return "Output will have worst enchantment from each ingredient with that enchantment.";
            case ADDITIVE:
                return "The enchantments of all ingredients will add together.";

            default: return "Unknown behaviour. Add description in net.Indyuce.mmoitems.gui.edition.recipe.rba.RBA_SmithingEnchantments";
        }
    }
}
