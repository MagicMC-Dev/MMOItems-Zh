package net.Indyuce.mmoitems.comp.mmocore.stat;

import net.Indyuce.mmocore.api.player.attribute.PlayerAttribute;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

public class ExtraAttribute extends DoubleStat {
    public ExtraAttribute(PlayerAttribute attribute) {
        super("ADDITIONAL_" + attribute.getId().toUpperCase().replace("-", "_"), Material.LIME_DYE,
                "Additional " + attribute.getName() + " (MMOCore)", new String[]{"Amount of " + attribute.getName() + " points the player",
                        "gets when holding/wearing this item."}, new String[]{"!block", "all"});
    }

    @Override
    @Deprecated
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull DoubleData data) {

        // Lore Management
        int lvl = (int) Math.round(data.getValue());
        item.getLore().insert(getPath(), DoubleStat.formatPath(getPath(), getGeneralStatFormat(), false, false, lvl));

        // Insert NBT
        item.addItemTag(getAppliedNBT(data));
    }
}
