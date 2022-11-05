package net.Indyuce.mmoitems.gui.edition.recipe.rba;

import io.lumine.mythic.lib.api.util.ui.QuickNumberRange;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import io.lumine.mythic.lib.api.util.ItemFactory;
import net.Indyuce.mmoitems.gui.edition.recipe.rba.type.RBA_DoubleButton;
import net.Indyuce.mmoitems.gui.edition.recipe.gui.RecipeMakerGUI;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RBA_CookingTime extends RBA_DoubleButton {

    /**
     * A button of an Edition Inventory. Nice!
     *
     * @param inv The edition inventory this is a button of
     */
    public RBA_CookingTime(@NotNull RecipeMakerGUI inv) { super(inv); }

    public static final String FURNACE_TIME = "time";
    @NotNull @Override public String getDoubleConfigPath() { return FURNACE_TIME; }

    @Nullable @Override public QuickNumberRange getRange() { return new QuickNumberRange(0D, null); }

    @Override public boolean requireInteger() { return true; }

    public static final double DEFAULT = 200;
    @Override public double getDefaultValue() { return DEFAULT; }

    @NotNull final ItemStack doubleButton = RecipeMakerGUI.addLore(ItemFactory.of(Material.CLOCK).name("\u00a7cDuration").lore(SilentNumbers.chop(
            "How long it takes this recipe to finish 'cooking' x)"
            , 65, "\u00a77")).build(), SilentNumbers.toArrayList(""));
    @NotNull @Override public ItemStack getDoubleButton() { return doubleButton; }
}
