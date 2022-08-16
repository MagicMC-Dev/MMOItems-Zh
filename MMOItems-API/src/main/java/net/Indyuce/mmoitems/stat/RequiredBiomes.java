package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.stat.data.StringListData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import net.Indyuce.mmoitems.stat.type.ItemRestriction;
import net.Indyuce.mmoitems.stat.type.StringListStat;
import org.bukkit.Material;

import java.util.ArrayList;

/**
 * @author Gunging
 */
public class RequiredBiomes extends StringListStat implements ItemRestriction, GemStoneStat {
    public RequiredBiomes() {
        super("REQUIRED_BIOMES",  Material.JUNGLE_SAPLING, "Required Biomes", new String[] { "The biome the player must be within", "for this item to activate." }, new String[] { "!block", "all" });
    }

    @Override
    public boolean canUse(RPGPlayer player, NBTItem item, boolean message) {

        // bruh
        if (!item.hasTag(getNBTPath())) { return true; }

        // Find the relevant tags
        ArrayList<ItemTag> relevantTags = new ArrayList<>();
        if (item.hasTag(getNBTPath())) { relevantTags.add(ItemTag.getTagAtPath(getNBTPath(), item, SupportedNBTTagValues.STRING)); }

        // Generate data
        StringListData data = (StringListData) getLoadedNBT(relevantTags);

        boolean counter = false;
        if (data != null) {

            // Check every string, must match once
            for (String biome : data.getList()) {

                // Crop
                String tst = biome.toLowerCase().replace(" ", "_").replace("-", "_");
                if (tst.startsWith("!")) { counter = true; tst = tst.substring(1); }

                // Get biome
                String b = player.getPlayer().getLocation().getBlock().getBiome().getKey().getKey();

                // Check
                if (b.contains(tst)) { return !counter; }
            }

            // If the biome evaded all restrictions it will return true; if the biome didn't met any specifications it will be false.
            return counter;
        }

        return true;
    }

    @Override
    public boolean isDynamic() { return true; }
}
