package net.Indyuce.mmoitems.stat.type;

import net.Indyuce.mmoitems.api.player.PlayerData;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;

/**
 * Since MMOItems 6.7 attribute stats are now fully handled by MythicLib.
 *
 * @author jules
 * @see {@link #getOffset(PlayerData)} for class use case
 */
public abstract class AttackWeaponStat extends DoubleStat {
    private final Attribute attribute;

    public AttackWeaponStat(String id, Material mat, String name, String[] lore, Attribute attribute) {
        super(id, mat, name, lore, new String[]{"!consumable", "!block", "!miscellaneous", "all"});

        this.attribute = attribute;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    /**
     * Since the default attack speed is 4, holding a 2 atk speed
     * weapon actually LOWERS the player's attack speed. When a weapon
     * has 2 atk speed, it actually applies a modifier with value -2.
     * The difference between the 'apparent stat value' and the 'modifier
     * value' is equal to the player's base attribute value.
     * <p>
     * This method used to return a constant but now requires a player
     * as parameter because the offset depends on the player's base
     * attribute value.
     * <p>
     * Before MI 6.7, MI used to consider that the base attribute value
     * was equal to the DEFAULT attribute value which is not always the case.
     * This generated issues when MMOCore changed the player's base attribute
     * value to non default values.
     *
     * @return Offset that needs to be substract from the apparent stat value
     */
    public double getOffset(PlayerData playerData) {
        return playerData.getPlayer().getAttribute(attribute).getBaseValue();
    }
}
