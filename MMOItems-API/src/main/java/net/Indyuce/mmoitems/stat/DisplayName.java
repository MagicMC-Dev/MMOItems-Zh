package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import io.lumine.mythic.lib.comp.adventure.AdventureParser;
import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import net.Indyuce.mmoitems.stat.type.NameData;
import net.Indyuce.mmoitems.stat.type.StatHistory;
import net.Indyuce.mmoitems.stat.type.StringStat;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class DisplayName extends StringStat implements GemStoneStat {

    public DisplayName() {
        super("NAME", VersionMaterial.OAK_SIGN.toMaterial(), "Display Name", new String[]{"The item display name."},
                new String[]{"all"});
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StringData data) {
        final ItemTier tier = item.getMMOItem().getTier();
        final AdventureParser parser = MythicLib.plugin.getAdventureParser();
        String format = data.toString();

        // Bake
        format = format.replace("<tier-name>", tier != null ? parser.stripColors(tier.getUnparsedName()) : "")
                .replace("<tier-color>", tier != null ? parser.lastColor(tier.getUnparsedName(), true) : "&f")
                .replace("<tier-color-cleaned>", tier != null ? parser.lastColor(tier.getUnparsedName(), false) : "");


        // Is this upgradable?
        format = cropUpgrade(format);
        if (item.getMMOItem().hasUpgradeTemplate())
            format = appendUpgradeLevel(format, item.getMMOItem().getUpgradeLevel());

        item.getMeta().setDisplayName(format);

        // Force Stat History generation
        StatHistory.from(item.getMMOItem(), this);

        // Add NBT
        item.addItemTag(getAppliedNBT(data));
    }

    @NotNull
    String cropUpgrade(@NotNull String format) {
        String suffix = MMOItems.plugin.getConfig().getString("item-upgrading.name-suffix", " &8(&e+#lvl#&8)");
        if (suffix == null || suffix.isEmpty())
            return format;

        //MMOItems.getConsole().sendMessage("Level " + upgradeLevel);
        //MMOItems.getConsole().sendMessage("Format " + format);


        // Crop lvl
        int lvlOFFSET = suffix.indexOf("#lvl#");
        if (lvlOFFSET < 0)
            return format;
        String sB4 = suffix.substring(0, lvlOFFSET);
        String aFt = suffix.substring(lvlOFFSET + "#lvl#".length());
        String sB4_alt = sB4.replace("+", "-");
        String aFt_alt = aFt.replace("+", "-");

        // Remove it
        if (format.contains(sB4)) {

            // Get offsets
            int sB4_offset = format.indexOf(sB4);
            int aFt_offset = format.lastIndexOf(aFt);

            // No after = to completion
            if (aFt_offset < 0) {
                aFt_offset = format.length();
            } else {
                aFt_offset += aFt.length();
            }

            // Remove that
            String beforePrefix = format.substring(0, sB4_offset);
            String afterPrefix = format.substring(aFt_offset);

            // Replace
            format = beforePrefix + afterPrefix;
        }

        // Remove it
        if (format.contains(sB4_alt)) {

            // Get offsets
            int sB4_offset = format.indexOf(sB4_alt);
            int aFt_offset = format.lastIndexOf(aFt_alt);

            // No after = to completion
            if (aFt_offset < 0) {
                aFt_offset = format.length();
            } else {
                aFt_offset += aFt_alt.length();
            }

            // Remove that
            String beforePrefix = format.substring(0, sB4_offset);
            String afterPrefix = format.substring(aFt_offset);

            // Replace
            format = beforePrefix + afterPrefix;
        }

				/*/ Bake old indices for removal
				ArrayList<String> oldSuffixii = new ArrayList<>(); boolean negativity = false;
				if (upgradeLevel < 0) { upgradeLevel = -upgradeLevel; negativity = true; }
				for (int i = upgradeLevel + 3; i >= 1; i--) {
					if (negativity) {
						oldSuffixii.add(levelPrefix(suffix, -i));
					} else {
						oldSuffixii.add(levelPrefix(suffix, i)); } }
				for (String str : oldSuffixii) {

					//MMOItems.getConsole().sendMessage("Found " + str);

					// Remove the one with color parsed from the current name
					format = format.replace(str, "");

					//MMOItems.getConsole().sendMessage("Edited " + format);
				} //*/

        //MMOItems.getConsole().sendMessage("Final " + format);

        return format;
    }

    @NotNull
    public static String appendUpgradeLevel(@NotNull String format, int lvl) {
        String suffix = MMOItems.plugin.getConfig().getString("item-upgrading.name-suffix");
        if (suffix != null && lvl != 0) {
            String actSuffix = levelPrefix(suffix, lvl);
            return format + actSuffix;
        }
        return format;
    }

    @NotNull
    public static String levelPrefix(@NotNull String template, int toLevel) {
        return template
                .replace("#lvl#", String.valueOf(toLevel))
                .replace("+-", "-");
    }

    /**
     * This is not saved as a custom NBT data, instead it is stored as the name of the item itself.
     * Alas this returns an empty list
     */
    @NotNull
    @Override
    public ArrayList<ItemTag> getAppliedNBT(@NotNull StringData data) {

        if (data instanceof NameData) {

            ArrayList<ItemTag> tags = new ArrayList<>();

            // Append those
            tags.add(new ItemTag(getNBTPath(), ((NameData) data).getMainName()));
            if (((NameData) data).hasPrefixes()) {
                tags.add(((NameData) data).compressPrefixes(getNBTPath() + "_PRE"));
            }
            if (((NameData) data).hasSuffixes()) {
                tags.add(((NameData) data).compressSuffixes(getNBTPath() + "_SUF"));
            }

            return tags;
        }

        // Thats it
        return new ArrayList<>();
    }

    @Override
    public void whenLoaded(@NotNull ReadMMOItem mmoitem) {

        // Get tags
        ArrayList<ItemTag> relevantTags = new ArrayList<>();
        boolean stored = false;
        ItemTag mainName = ItemTag.getTagAtPath(getNBTPath(), mmoitem.getNBT(), SupportedNBTTagValues.STRING);

        //NME//MMOItems.log("\u00a7b\u00a2\u00a2\u00a2\u00a77 Loading name of \u00a7b" + mmoitem.getType() + " " + mmoitem.getId());

        if (mainName != null) {

            // Ah yes
            ItemTag prefixes = ItemTag.getTagAtPath(getNBTPath() + "_PRE", mmoitem.getNBT(), SupportedNBTTagValues.STRING);
            ItemTag suffixes = ItemTag.getTagAtPath(getNBTPath() + "_SUF", mmoitem.getNBT(), SupportedNBTTagValues.STRING);
            relevantTags.add(mainName);
            relevantTags.add(prefixes);
            relevantTags.add(suffixes);

            // No need to evaluate anvil changes if the item has no display name
            if (mmoitem.getNBT().getItem().getItemMeta().hasDisplayName()) {
                stored = true;
            }

        } else {

            // No need to continue if the item has no display name
            if (!mmoitem.getNBT().getItem().getItemMeta().hasDisplayName()) {
                return;
            }

            //NME//MMOItems.log("\u00a7a\u00a2\u00a2\u00a2\u00a77 Older item, decrypting as main name as:\u00a7f " + cropUpgrade(mmoitem.getNBT().getItem().getItemMeta().getDisplayName()));

            // Add sole tag
            relevantTags.add(new ItemTag(getNBTPath(), cropUpgrade(mmoitem.getNBT().getItem().getItemMeta().getDisplayName())));
        }

        // Use that
        NameData bakedData = (NameData) getLoadedNBT(relevantTags);

        // Valid?
        if (bakedData != null) {

            //NME//MMOItems.log("\u00a7e\u00a2\u00a2\u00a2\u00a77 Built:\u00a7f " + bakedData.toString());
            /*
             * Suppose we expect an item name with prefixes and suffixes,
             * well, removing those should leave the bare name, right?
             *
             * If the player has renamed their item, this bare name will be somewhat
             * different, and this is where those changes are updated.
             */

            @Nullable String itemName = null;
            if (stored) {

                // Could the player have renamed?
                itemName = mmoitem.getNBT().getItem().getItemMeta().getDisplayName();
                String colorless = ChatColor.stripColor(itemName);

                //NME//MMOItems.log("\u00a7b\u00a2\u00a2\u00a2\u00a77 Comparing: " + itemName + " | " + colorless);
                // By player
                if (!itemName.equals(colorless)) {
                    //NME//MMOItems.log("\u00a7b\u00a2\u00a2\u00a2\u00a77 Not anvil");
                    itemName = null;

                } else {
                    //NME//MMOItems.log("\u00a7b\u00a2\u00a2\u00a2\u00a77 Replaced main with \u00a7b " + itemName);
                    bakedData.setString(itemName);
                }

            }

            // Set
            mmoitem.setData(this, bakedData);

            // Update in SH. Must happen after setting the data
            if (stored && itemName != null) {

                // History not prematurely loaded?
                if (mmoitem.getStatHistory(this) == null) {

                    // Also load history :think ing:
                    ItemTag hisTag = ItemTag.getTagAtPath(ItemStackBuilder.history_keyword + getId(), mmoitem.getNBT(), SupportedNBTTagValues.STRING);

                    if (hisTag != null) {

                        // Aye
                        StatHistory hist = StatHistory.fromNBTString(mmoitem, (String) hisTag.getValue());

                        // History valid? Record
                        if (hist != null) {

                            // Original Data
                            NameData og = (NameData) hist.getOriginalData();

                            // Overwrite
                            og.setString(itemName);

                            // Load its stat history
                            mmoitem.setStatHistory(this, hist);

                            //NME//MMOItems.log("\u00a7b\u00a2\u00a2\u00a2\u00a77 Name History:");
                            //NME//hist.log();
                        }
                    }
                }
            }
        }
    }

    @Nullable
    @Override
    public StringData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {

        // You got a double right
        ItemTag tg = ItemTag.getTagAtPath(getNBTPath(), storedTags);

        // Found righ
        if (tg != null) {

            // Get number
            String value = (String) tg.getValue();

            // That's it
            NameData nd = new NameData(value);

            nd.readPrefixes(ItemTag.getTagAtPath(getNBTPath() + "_PRE", storedTags));
            nd.readSuffixes(ItemTag.getTagAtPath(getNBTPath() + "_SUF", storedTags));

            return nd;
        }

        // Fail
        return null;
    }

    @NotNull
    @Override
    public StringData getClearStatData() {
        return new NameData("");
    }

    @Override
    public StringData whenInitialized(Object object) {
        return new NameData(object.toString());
    }
}
