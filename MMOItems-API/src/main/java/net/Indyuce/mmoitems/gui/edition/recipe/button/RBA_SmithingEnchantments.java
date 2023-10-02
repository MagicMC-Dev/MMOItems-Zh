package net.Indyuce.mmoitems.gui.edition.recipe.button;

import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import io.lumine.mythic.lib.api.util.ItemFactory;
import net.Indyuce.mmoitems.api.crafting.recipe.SmithingCombinationType;
import net.Indyuce.mmoitems.gui.edition.recipe.button.type.RBA_ChooseableButton;
import net.Indyuce.mmoitems.gui.edition.recipe.gui.RecipeEditorGUI;
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
    public RBA_SmithingEnchantments(@NotNull RecipeEditorGUI inv) { super(inv); }

    @NotNull final ItemStack chooseableButton = ItemFactory.of(Material.ENCHANTING_TABLE).name("\u00a7a附魔转移").lore(SilentNumbers.chop(
            "原物品的附魔效果会怎样？附魔的原物品会产生附魔的产出物品吗？"
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
                return "对于每个附魔,将取该附魔在所有物品中的平均等级";
            case NONE:
                return "将忽略任何物品的附魔效果..";
            case MAXIMUM:
                return "输出将具有每种物品的最佳附魔";
            case MINIMUM:
                return "输出的每种物品的最差附魔效果";
            case ADDITIVE:
                return "所有物品的附魔将叠加在一起";

            default: return "未知行为. 在 net.Indyuce.mmoitems.gui.edition.recipe.rba.RBA_SmithingEnchantments 中添加行为";
        }
    }
}
