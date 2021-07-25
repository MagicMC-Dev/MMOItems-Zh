package net.Indyuce.mmoitems.comp.enchants;

import io.lumine.mythic.lib.api.item.ItemTag;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.InternalStat;
import net.Indyuce.mmoitems.stat.type.StringStat;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AdvancedEnchantsStat extends StringStat implements InternalStat {
    public AdvancedEnchantsStat() {
        super("ADVANCED_ENCHANTS", Material.BOOK, "Advanced Enchants", new String[0], new String[]{"all"});
    }

    @Override
    public RandomStatData whenInitialized(Object object) {
        // Not supported
        return null;
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StatData data) {





    }

    @Override
    public void whenLoaded(@NotNull ReadMMOItem mmoitem) {

    }

    @NotNull
    @Override
    public ArrayList<ItemTag> getAppliedNBT(@NotNull StatData data) {
        return new ArrayList<>();
    }

    @Override
    public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
        // Not supported
    }

    @Override
    public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
        // Not supported
    }

    @Nullable
    @Override
    public StatData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {
        return null;
    }

    @Override
    public void whenDisplayed(List<String> lore, Optional<RandomStatData> statData) {
        // Not supported
    }

    @NotNull
    @Override
    public StatData getClearStatData() {
        // Not supported
        return null;
    }
}
