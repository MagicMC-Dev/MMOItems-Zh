package net.Indyuce.mmoitems.comp.mmocore.stat;

import net.Indyuce.mmocore.api.player.attribute.PlayerAttribute;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import org.bukkit.Material;

public class ExtraAttribute extends DoubleStat {
    public ExtraAttribute(PlayerAttribute attribute) {
        super("ADDITIONAL_" + attribute.getId().toUpperCase().replace("-", "_"), Material.LIME_DYE,
                "Additional " + attribute.getName() + " (MMOCore)", new String[]{"Amount of " + attribute.getName() + " points the player",
                        "gets when holding/wearing this item."}, new String[]{"!block", "all"});
    }
}
