package net.Indyuce.mmoitems.comp;

import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackProvider;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.api.util.message.FFPMMOItems;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StringListData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.StringListStat;
import net.advancedplugins.ae.enchanthandler.enchantments.AEnchants;
import net.advancedplugins.ae.enchanthandler.enchantments.AdvancedEnchantment;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * List of enchantments in an item yes
 */
public class AdvancedEnchantsStat extends StringListStat {

    public AdvancedEnchantsStat() {
        super("ADVANCED_ENCHANTS", VersionMaterial.EXPERIENCE_BOTTLE.toMaterial(), "Advanced Enchants", new String[]{"The AEnchants of this. Format:", "\u00a7e[internal_name] [level]"}, new String[]{"!miscellaneous", "!block", "all"});
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StatData data) {

        /*
         * Now just apply all of them as AEnchants
         */
        StringListData sList = (StringListData) data;

        // Do that
        HashMap<String, Integer> aes = readList(sList.getList());
        ArrayList<String> loreInserts = new ArrayList<>();

        // Dont repeat
        ArrayList<String> repeats = new ArrayList<>();

        // Enchant the item
        for (String ench : aes.keySet()) {

            Integer lvl = aes.get(ench);

            // Skip trash
            if (ench == null || lvl == null || ench.isEmpty()) { continue; }
            if (!AEnchants.getEnchants().contains(ench.toLowerCase())) { continue; }

            // Find the actual enchantment and include lore
            AdvancedEnchantment instance = new AdvancedEnchantment(ench);

            // Exceed max level? Nope
            if (!instance.getLevelList().contains(lvl)) { continue; }

            // Skip repetitions
            if (repeats.contains(ench.toLowerCase())) { continue; }
            repeats.add(ench.toLowerCase());

            String display = instance.getDisplay(lvl) + " " + SilentNumbers.toRomanNumerals(lvl);

            // Add lore
            loreInserts.add(display);
            // Add the tag
            item.addItemTag(tagForAEnchantment(ench, lvl));
        }

        item.getLore().insert(getPath(), loreInserts);
    }

    @Override
    public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
        List<String> list = inv.getEditedSection().contains(getPath()) ? inv.getEditedSection().getStringList(getPath()) : new ArrayList<>();

        // Get space
        AEListEnchantment fromMessage = readEntry(message);
        Validate.notNull(fromMessage, FriendlyFeedbackProvider.quickForPlayer(FFPMMOItems.get(),
                "Incorrect format, use $e[internal_name] [level]$b. For the list of enchantments, use $r/ae list$b. "));

        Validate.isTrue(AEnchants.getEnchants().contains(fromMessage.getName().toLowerCase()), FriendlyFeedbackProvider.quickForPlayer(FFPMMOItems.get(),
                "Adv Enchantment '$u{0}$b' doesnt exist. For the list of enchantments, use $r/ae list$b. ", fromMessage.getName()));

        // Find the actual enchantment to compare level
        AdvancedEnchantment instance = new AdvancedEnchantment(fromMessage.getName());

        Validate.isTrue(instance.getLevelList().contains(fromMessage.getLevel()), FriendlyFeedbackProvider.quickForPlayer(FFPMMOItems.get(),
                "Adv Enchantment '$u{0}$b' cannot be level $u{1}$b. It only admits levels $e{2}$b. ", fromMessage.getName(), String.valueOf(fromMessage.getLevel()),
                SilentNumbers.collapseList(SilentNumbers.transcribeList(instance.getLevelList(), String::valueOf), "$b,$e ")));

        // Level is valid, lets remove old enchantment from this list
        for (int s = 0; s < list.size(); s++) {

            // Get that
            AEListEnchantment atList = readEntry(list.get(s));

            // Skip
            if (atList == null) { continue; }

            // Same? remove
            if (atList.getName().toLowerCase().equals(fromMessage.getName().toLowerCase())) {

                // Remove and repeat index
                list.remove(s);
                s--;
            }
        }

        // Include the message
        list.add(message);

        inv.getEditedSection().set(getPath(), list);
        inv.registerTemplateEdition();
        inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + getName() + " Stat successfully added.");
    }

    /**
     *
     * @param name Name of the AEnch ~ arrow_deflect
     * @param level Level of the AEnch ~ 4
     * @return The tag ~ "ae_enchantment;arrow_deflect": 4
     */
    ItemTag tagForAEnchantment(@NotNull String name, int level) { return new ItemTag(AE_TAG + ";" + name, level); }

    @NotNull public static final String AE_TAG = "ae_enchantment";

    @Override
    public void whenLoaded(@NotNull ReadMMOItem mmoitem) {

        // Look at all tags that start with ae_enchantment I guess
        ArrayList<ItemTag> relevantTags = new ArrayList<>();
        for (String tag : mmoitem.getNBT().getTags()) {

            // Valid tag?
            if (tag == null) { continue; }
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
        if (data != null) {

            /*
             * This data has the enchantments that used to be stored in the item.
             */

            mmoitem.setData(this, data);
        }
    }

    @Nullable
    @Override
    public StatData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {

        ArrayList<String> aEnchs = new ArrayList<>();

        // Yes
        for (ItemTag tag : storedTags) {
            if (tag == null) { continue; }
            if (!(tag.getValue() instanceof Integer)) { continue; }

            // Path must be valid
            int spc = tag.getPath().indexOf(AE_TAG + ";");
            if (spc < 0) { continue; }

            // Crop
            String enchantment = tag.getPath().substring(AE_TAG.length() + 1 + spc);
            int value = (int) tag.getValue();

            // Make String
            String str = enchantment + " " + SilentNumbers.removeDecimalZeros(String.valueOf(value));
            aEnchs.add(str);
        }

        if (aEnchs.size() == 0) {

            // Compatibility with StatHistory probably
            return super.getLoadedNBT(storedTags);
        }


        // THats it
        return new StringListData(aEnchs);
    }

    @NotNull HashMap<String, Integer> readList(@NotNull List<String> listed) {

        HashMap<String, Integer> ret = new HashMap<>();

        // Split all by like NAME
        for (String str : listed) {

            // List
            AEListEnchantment aeLE = readEntry(str);

            // Skip trash
            if (aeLE == null) { continue; }

            // Put
            ret.put(aeLE.getName(), aeLE.getLevel());
        }

        return ret;
    }

    @Nullable AEListEnchantment readEntry(@Nullable String str) {

        // Skip trash
        if (str == null || !str.contains(" ")) { return null; }

        // Split by integer
        int spc = str.lastIndexOf(' ');
        String level = str.substring(spc + 1);

        // Parse that
        Integer lvl = SilentNumbers.IntegerParse(level);

        // Not null right
        if (lvl == null) { return null; }

        // Add that
        String name = str.substring(0, spc);
        return new AEListEnchantment(name, lvl);
    }

    private static class AEListEnchantment {

        @NotNull final String name;
        final int level;

        AEListEnchantment(@NotNull String name, int level) {
            this.name = name;
            this.level = level;
        }

        @NotNull public String getName() { return name; }
        public int getLevel() { return level; }

        @Override public String toString() { return getName() + " " + getLevel(); }
    }
}
