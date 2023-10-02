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
    public RBA_SmithingUpgrades(@NotNull RecipeEditorGUI inv) { super(inv); }

    @NotNull final ItemStack chooseableButton = ItemFactory.of(Material.ANVIL).name("\u00a7a升级转移").lore(SilentNumbers.chop(
            "物品升级后会发生什么变化?升级后的原物品(没升级的物品)会产生升级后的输出物品吗?"
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
                return "将取合并物品升级等级的平均值";
            case NONE:
                return "将忽略任何材料的升级等级";
            case MAXIMUM:
                return "输出物品将具有最高升级材料的升级等级";
            case MINIMUM:
                return "输出物品将具有最低升级的物品升级等级";
            case ADDITIVE:
                return "将添加原物品的升级等级, 结果将是制作物品的等级";

            default: return "未知的行为,在 net.Indyuce.mmoitems.gui.edition.recipe.rba.RBA_SmithingUpgrades 中添加";
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
