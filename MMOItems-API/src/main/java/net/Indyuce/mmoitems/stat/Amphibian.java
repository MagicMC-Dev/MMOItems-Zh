package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.type.ChooseStat;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import net.Indyuce.mmoitems.stat.type.ItemRestriction;
import net.Indyuce.mmoitems.util.StatChoice;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * @author Gunging
 */
public class Amphibian extends ChooseStat implements ItemRestriction, GemStoneStat {
    public static final StatChoice
            NORMAL = new StatChoice("NORMAL", "No liquids dependency"),
            DRY = new StatChoice("DRY", "The item does not work if the player is touching a liquid block."),
            WET = new StatChoice("WET", "The only works if the player is touching water (rain does not count)."),
            DAMP = new StatChoice("DAMP", "The only works if the player is completely submerged in water."),
            LAVA = new StatChoice("LAVA", "The only works if the player is touching lava."),
            MOLTEN = new StatChoice("MOLTEN", "The only works if the player is completely submerged in lava."),
            LIQUID = new StatChoice("LIQUID", "The only works if the player is touching any liquid."),
            SUBMERGED = new StatChoice("SUBMERGED", "The only works if the player is completely submerged in any liquid.");

    public Amphibian() {
        super("AMPHIBIAN", Material.WATER_BUCKET, "Amphibian", new String[]{"May this item only be used in specific", "environments regarding liquids?"}, new String[]{"!block", "all"});

        addChoices(NORMAL, DRY, WET, DAMP, LAVA, MOLTEN, LIQUID, SUBMERGED);
    }

    @NotNull
    @Override
    public StringData getClearStatData() {
        return new StringData(NORMAL.getId());
    }

    @Override
    public boolean canUse(RPGPlayer player, NBTItem item, boolean message) {

        // bruh
        if (!item.hasTag(getNBTPath())) {
            return true;
        }

        // Find the relevant tags
        ArrayList<ItemTag> relevantTags = new ArrayList<>();
        if (item.hasTag(getNBTPath())) {
            relevantTags.add(ItemTag.getTagAtPath(getNBTPath(), item, SupportedNBTTagValues.STRING));
        }

        // Generate data
        String data = getLoadedNBT(relevantTags).toString();

        // Well...
        if (data != null) {
            //BKK//MMOItems. Log(" \u00a7a>\u00a73>\u00a77 Checking Amphibian: \u00a7f" + item.getType() + " \u00a7b" + data.toString());

            // Switch
            switch (data) {
                case "NORMAL":
                default:
                    //BKK//MMOItems. Log(" \u00a7a>\u00a73>\u00a7a>\u00a77 Normal \u00a7aSucceed");
                    return true;
                case "DRY":
                    for (Block b : blocksTouchedByPlayer(player.getPlayer())) {
                        //BKK//MMOItems. Log(" \u00a77>\u00a77>\u00a73>\u00a77 Examining \u00a7f" + b.getType().toString());
                        if (b.isLiquid()) {
                            //BKK//MMOItems. Log(" \u00a7a>\u00a73>\u00a7a> \u00a7cFail");
                            return false;
                        }
                    }
                    //BKK//MMOItems. Log(" \u00a7a>\u00a73>\u00a7a> \u00a7aSucceed");
                    return true;
                case "WET":
                    for (Block b : blocksTouchedByPlayer(player.getPlayer())) {
                        //BKK//MMOItems. Log(" \u00a77>\u00a77>\u00a73>\u00a77 Examining \u00a7f" + b.getType().toString());
                        if (b.getType().equals(Material.WATER)) {
                            //BKK//MMOItems. Log(" \u00a7a>\u00a73>\u00a7a> \u00a7aSucceed");
                            return true;
                        }
                    }
                    //BKK//MMOItems. Log(" \u00a7a>\u00a73>\u00a7a> \u00a7cFail");
                    return false;
                case "DAMP":
                    for (Block b : blocksTouchedByPlayer(player.getPlayer())) {
                        //BKK//MMOItems. Log(" \u00a77>\u00a77>\u00a73>\u00a77 Examining \u00a7f" + b.getType().toString());
                        if (!b.getType().equals(Material.WATER)) {
                            //BKK//MMOItems. Log(" \u00a7a>\u00a73>\u00a7a> \u00a7cFail");
                            return false;
                        }
                    }
                    //BKK//MMOItems. Log(" \u00a7a>\u00a73>\u00a7a> \u00a7aSucceed");
                    return true;
                case "LAVA":
                    for (Block b : blocksTouchedByPlayer(player.getPlayer())) {
                        //BKK//MMOItems. Log(" \u00a77>\u00a77>\u00a73>\u00a77 Examining \u00a7f" + b.getType().toString());
                        if (b.getType().equals(Material.LAVA)) {
                            //BKK//MMOItems. Log(" \u00a7a>\u00a73>\u00a7a> \u00a7aSucceed");
                            return true;
                        }
                    }
                    //BKK//MMOItems. Log(" \u00a7a>\u00a73>\u00a7a> \u00a7cFail");
                    return false;
                case "MOLTEN":
                    for (Block b : blocksTouchedByPlayer(player.getPlayer())) {
                        //BKK//MMOItems. Log(" \u00a77>\u00a77>\u00a73>\u00a77 Examining \u00a7f" + b.getType().toString());
                        if (!b.getType().equals(Material.LAVA)) {
                            //BKK//MMOItems. Log(" \u00a7a>\u00a73>\u00a7a> \u00a7cFail");
                            return false;
                        }
                    }
                    //BKK//MMOItems. Log(" \u00a7a>\u00a73>\u00a7a> \u00a7aSucceed");
                    return true;
                case "LIQUID":
                    for (Block b : blocksTouchedByPlayer(player.getPlayer())) {
                        //BKK//MMOItems. Log(" \u00a77>\u00a77>\u00a73>\u00a77 Examining \u00a7f" + b.getType().toString());
                        if (b.isLiquid()) {
                            //BKK//MMOItems. Log(" \u00a7a>\u00a73>\u00a7a> \u00a7aSucceed");
                            return true;
                        }
                    }
                    //BKK//MMOItems. Log(" \u00a7a>\u00a73>\u00a7a> \u00a7cFail");
                    return false;
                case "SUBMERGED":
                    for (Block b : blocksTouchedByPlayer(player.getPlayer())) {
                        //BKK//MMOItems. Log(" \u00a77>\u00a77>\u00a73>\u00a77 Examining \u00a7f" + b.getType().toString());
                        if (!b.isLiquid()) {
                            //BKK//MMOItems. Log(" \u00a7a>\u00a73>\u00a7a> \u00a7cFail");
                            return false;
                        }
                    }
                    //BKK//MMOItems. Log(" \u00a7a>\u00a73>\u00a7a> \u00a7aSucceed");
                    return true;
            }
        }
        return true;
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    // Yes
    ArrayList<Block> blocksTouchedByPlayer(@NotNull Player p) {

        // Get Bounding Box
        BoundingBox box = p.getBoundingBox();

        // Ret
        ArrayList<Block> ret = new ArrayList<>();

        // Perform Summation
        for (double dx = box.getMinX(); dx <= box.getMaxX(); dx += Math.min(1, Math.max(box.getWidthX() - (dx - box.getMinX()), 0.001))) {
            for (double dy = box.getMinY(); dy <= box.getMaxY(); dy += Math.min(1, Math.max(box.getHeight() - (dy - box.getMinY()), 0.001))) {
                for (double dz = box.getMinZ(); dz <= box.getMaxZ(); dz += Math.min(1, Math.max(box.getWidthZ() - (dz - box.getMinZ()), 0.001))) {

                    // Exclusion
                    int dxI = SilentNumbers.floor(dx);
                    int dyI = SilentNumbers.floor(dy);
                    int dzI = SilentNumbers.floor(dz);

                    //BKK//MMOItems. Log("  \u00a77at \u00a76" + dxI + " " + dyI + " " + dzI + "\u00a78 (" + dx + " " + dy + " " + dz + ")");

                    // Get block
                    Block bkk = p.getLocation().getWorld().getBlockAt(dxI, dyI, dzI);

                    // Contained already?
                    if (!ret.contains(bkk)) {
                        //BKK//MMOItems. Log("  \u00a7a  > \u00a77Added");

                        // Get block at
                        ret.add(bkk);
                    }
                }
            }
        }

        //BKK//MMOItems. Log("Total: \u00a7e" + ret.size() + "\u00a77 blocks.");
        return ret;
    }
}
