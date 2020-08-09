package net.Indyuce.mmoitems.comp.mythicmobs.stat;

import net.Indyuce.mmoitems.stat.type.DoubleStat;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class FactionDamage extends DoubleStat {

    public FactionDamage(String faction) {
        super("FACTION_DAMAGE_" + faction.toUpperCase(), new ItemStack(Material.RED_DYE), faction + " Faction Damage", new String[] { "Deals additional damage to mobs", "from the " + faction + " faction in %." }, new String[] { "!block", "all" });
    }
}

