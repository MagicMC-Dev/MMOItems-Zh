package net.Indyuce.mmoitems.stat.type;

import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.element.Element;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.util.ElementStatType;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Used for elements otherwise MMOItems doesn't
 * register elemental stats inside of MythicLib
 * <p>
 * Workaround that will be removed when adding stat categories
 * in order to give more clarity to the item editor.
 *
 * @deprecated Definitely not a perfect implementation
 */
@Deprecated
public class FictiveNumericStat extends DoubleStat implements InternalStat {
    public FictiveNumericStat(Element el, ElementStatType type) {
        super(type.getConcatenatedTagPath(el), Material.BARRIER, "Fictive Stat", new String[0]);
    }

    @Override
    public NumericStatFormula whenInitialized(Object object) {
        throw new RuntimeException("Fictive item stat");
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull DoubleData data) {
        throw new RuntimeException("Fictive item stat");
    }

    @NotNull
    @Override
    public ArrayList<ItemTag> getAppliedNBT(@NotNull DoubleData data) {
        throw new RuntimeException("Fictive item stat");
    }

    @Override
    public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
        throw new RuntimeException("Fictive item stat");
    }

    @Override
    public void whenLoaded(@NotNull ReadMMOItem mmoitem) {
        throw new RuntimeException("Fictive item stat");
    }

    @Nullable
    @Override
    public DoubleData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {
        throw new RuntimeException("Fictive item stat");
    }

    @Override
    public void whenDisplayed(List<String> lore, Optional<NumericStatFormula> statData) {
        throw new RuntimeException("Fictive item stat");
    }
}
