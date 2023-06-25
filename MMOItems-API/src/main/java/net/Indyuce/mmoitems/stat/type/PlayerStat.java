package net.Indyuce.mmoitems.stat.type;

import org.bukkit.Material;


/**
 * @deprecated Not used yet. This should be a class which tells MMOItems to
 *         send the numeric stat value other to MythicLib player stats.
 */
@Deprecated
public class PlayerStat extends DoubleStat {
    public PlayerStat(String id, Material mat, String name, String[] lore) {
        this(id, mat, name, lore, new String[]{"!miscellaneous", "!block", "all"}, true);
    }

    public PlayerStat(String id, Material mat, String name, String[] lore, String[] types, Material... materials) {
        this(id, mat, name, lore, types, true, materials);
    }

    public PlayerStat(String id, Material mat, String name, String[] lore, String[] types, boolean moreIsBetter, Material... materials) {
        super(id, mat, name, lore, types, moreIsBetter, materials);
    }
}
