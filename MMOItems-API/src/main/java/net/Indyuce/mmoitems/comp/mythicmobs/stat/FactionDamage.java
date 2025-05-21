package net.Indyuce.mmoitems.comp.mythicmobs.stat;

import net.Indyuce.mmoitems.stat.type.DoubleStat;
import org.bukkit.Material;

// Faction damage is actually implemented in ML hook
public class FactionDamage extends DoubleStat {
    public FactionDamage(String faction) {
        super("FACTION_DAMAGE_" + faction.toUpperCase(), Material.RED_DYE, faction + " Faction Damage", new String[]{"Deals additional damage to mobs", "from the " + faction + " faction in %.", "", "Keep in mind that faction names are case", "insensitive for MMOItems. For example, the", "factions 'faction' and 'fAction' will share", "the same MMOItems faction damage stat."}, new String[]{"!block", "all"});
    }
}

