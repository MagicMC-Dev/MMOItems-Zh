package net.Indyuce.mmoitems.comp.enchants;

import io.lumine.mythic.lib.api.item.ItemTag;
import me.badbones69.crazyenchantments.Methods;
import me.badbones69.crazyenchantments.api.CrazyEnchantments;
import me.badbones69.crazyenchantments.api.objects.CEnchantment;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.InternalStat;
import net.Indyuce.mmoitems.stat.type.StringStat;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CrazyEnchantsStat extends StringStat implements InternalStat {

    /**
     * CrazyEnchants has no NBTTag where it stores enchant data.
     * Instead everything is stored in the item lore.
     * <p>
     * This class is an internal stat which makes sure crazy enchants
     * are loaded and added back to the item when building it again.
     */
    public CrazyEnchantsStat() {
        super("CRAZY_ENCHANTS", Material.BOOK, "Advanced Enchants", new String[0], new String[]{"all"});
    }

    /**
     * Rip off the {@link CrazyEnchantments#addEnchantments(ItemStack, Map)} method
     * https://github.com/badbones69/Crazy-Enchantments/blob/v1.8/plugin/src/main/java/me/badbones69/crazyenchantments/api/CrazyEnchantments.java
     */
    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StatData data) {
        Map<CEnchantment, Integer> enchants = ((CrazyEnchantsData) data).getEnchants();

        for (Map.Entry<CEnchantment, Integer> entry : enchants.entrySet()) {
            CEnchantment enchantment = entry.getKey();
            int level = entry.getValue();
            item.getLore().insert(0, Methods.color(enchantment.getColor() + enchantment.getCustomName() + " " + CrazyEnchantments.getInstance().convertLevelString(level)));
        }
    }

    @Override
    public void whenLoaded(@NotNull ReadMMOItem mmoitem) {
        Map<CEnchantment, Integer> enchants = CrazyEnchantments.getInstance().getEnchantments(mmoitem.getNBT().getItem());
        if (enchants.size() > 0)
            mmoitem.setData(this, new CrazyEnchantsData(enchants));
    }

    @Override
    public RandomStatData whenInitialized(Object object) {
        // Not supported
        return null;
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

    @Override
    public StatData getClearStatData() {
        return new CrazyEnchantsData(new HashMap<>());
    }
}
