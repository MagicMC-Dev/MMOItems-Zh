package net.Indyuce.mmoitems.stat.type;

import org.bukkit.Material;

public class EvilDoubleStat extends DoubleStat {

    public EvilDoubleStat(String id, Material mat, String name, String[] lore) { super(id, mat, name, lore); }
    public EvilDoubleStat(String id, Material mat, String name, String[] lore, String[] types, Material... materials) { super(id, mat, name, lore, types, materials); }

    @Override public boolean moreIsBetter() { return false; }
}
