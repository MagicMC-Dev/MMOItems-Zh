package net.Indyuce.mmoitems.comp.enchants.advanced_enchants;

import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.InternalStat;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.advancedplugins.ae.enchanthandler.enchantments.AEnchants;
import net.advancedplugins.ae.enchanthandler.enchantments.AdvancedEnchantment;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * List of enchantments in an item yes
 */
public class AdvancedEnchantsStat extends ItemStat<RandomStatData<AdvancedEnchantMap>, AdvancedEnchantMap> implements InternalStat {
    public AdvancedEnchantsStat() {
        super("ADVANCED_ENCHANTS", VersionMaterial.EXPERIENCE_BOTTLE.toMaterial(), "Advanced Enchants", new String[]{"The AEnchants of this item. Format:", "\u00a7e[internal_name] [level]"}, new String[]{"!miscellaneous", "!block", "all"});
    }

    @Override
    public RandomStatData whenInitialized(Object object) {
        throw new RuntimeException("Not supported");
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull AdvancedEnchantMap data) {

        // Do that
        Map<String, Integer> aes = data.enchants;

        // Enchant the item
        for (String ench : aes.keySet()) {

            Integer lvl = aes.get(ench);

            // Skip trash
            if (ench == null || lvl == null || ench.isEmpty())
                continue;

            // Find the actual enchantment and include lore
            AdvancedEnchantment instance = AEnchants.matchEnchant(ench);
            if (instance == null)
                return;

            // Add lore and tag
            item.getLore().insert(0, instance.getDisplay(lvl));
            item.addItemTag(getEnchantTag(ench, lvl));
        }
    }

    @NotNull
    @Override
    public ArrayList<ItemTag> getAppliedNBT(@NotNull AdvancedEnchantMap data) {
        ArrayList<ItemTag> array = new ArrayList<>();
        Map<String, Integer> aes = ((AdvancedEnchantMap) data).enchants;

        for (String ench : aes.keySet())
            array.add(getEnchantTag(ench, aes.get(ench)));

        return array;
    }

    @Override
    public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
        // Not supported
    }

    @Override
    public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
        // Not supported
    }

    @Override
    public void whenDisplayed(List<String> lore, Optional<RandomStatData<AdvancedEnchantMap>> statData) {
        // Not supported
    }

    @NotNull
    @Override
    public AdvancedEnchantMap getClearStatData() {
        return new AdvancedEnchantMap();
    }

    private static final String AE_TAG = "ae_enchantment";

    /**
     * @param name  Name of the AEnch ~ arrow_deflect
     * @param level Level of the AEnch ~ 4
     * @return The tag ~ "ae_enchantment;arrow_deflect": 4
     */
    private ItemTag getEnchantTag(@NotNull String name, int level) {
        return new ItemTag(AE_TAG + ";" + name, level);
    }

    @Override
    public void whenLoaded(@NotNull ReadMMOItem mmoitem) {

        // Look at all tags that start with ae_enchantment I guess
        ArrayList<ItemTag> relevantTags = new ArrayList<>();
        for (String tag : mmoitem.getNBT().getTags()) {

            // Valid tag?
            if (tag == null) {
                continue;
            }
            if (tag.startsWith(AE_TAG)) {

                ItemTag thatTag = ItemTag.getTagAtPath(tag, mmoitem.getNBT(), SupportedNBTTagValues.DOUBLE);

                // Not null?
                if (thatTag != null) {

                    relevantTags.add(new ItemTag(tag, SilentNumbers.round((Double) thatTag.getValue())));
                }
            }
        }

        // Generate data
        StatData data = getLoadedNBT(relevantTags);

        // Valid?
        if (data != null)

            // This data has the enchantments that used to be stored in the item.
            mmoitem.setData(this, data);
    }

    @Nullable
    @Override
    public AdvancedEnchantMap getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {
        AdvancedEnchantMap enchants = new AdvancedEnchantMap();

        // Yes
        for (ItemTag tag : storedTags) {
            if (tag == null || !(tag.getValue() instanceof Integer))
                continue;

            // Path must be valid
            int spc = tag.getPath().indexOf(AE_TAG + ";");
            if (spc < 0)
                continue;

            // Crop
            String enchantment = tag.getPath().substring(AE_TAG.length() + 1 + spc);
            int value = (int) tag.getValue();

            // Save enchant in map
            enchants.enchants.put(enchantment, value);
        }

        // Thats it
        return enchants.enchants.size() == 0 ? null : enchants;
    }
}
