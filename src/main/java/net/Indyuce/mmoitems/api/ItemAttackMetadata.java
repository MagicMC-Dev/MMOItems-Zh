package net.Indyuce.mmoitems.api;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.stat.StatMap;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.player.PlayerMetadata;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.bukkit.entity.LivingEntity;

/**
 * The attack metadata used here in MMOItems. It extends the default
 * attack metadata from MythicLib to benefit from all its methods
 *
 * @deprecated Elemental damage calculation will be moved to MythicLib and this
 *         class will therefore be 100% useless
 */
@Deprecated
public class ItemAttackMetadata extends AttackMetadata {
    public ItemAttackMetadata(DamageMetadata damage, PlayerMetadata damager) {
        super(damage, damager);
    }

    public ItemAttackMetadata(AttackMetadata attackMeta) {
        super(attackMeta.getDamage(), attackMeta);
    }

    public PlayerData getPlayerData() {
        return PlayerData.get(getPlayer().getUniqueId());
    }

    public ItemAttackMetadata clone() {
        return new ItemAttackMetadata(getDamage().clone(), this);
    }

    /**
     * Applies on-hit effects and deals damage to the target
     *
     * @param item   The item being used
     * @param target The entity target
     */
    public void applyEffectsAndDamage(NBTItem item, LivingEntity target) {
        MythicLib.plugin.getDamage().damage(applyEffects(item, target), target, true);
    }

    /**
     * Applies all necessary weapon on-hit effects for any type of damage.
     * Makes things much easier for untargeted weapons like staffs
     *
     * @param item   The item being used
     * @param target The entity target
     * @return The unedited attack result
     */
    public ItemAttackMetadata applyEffects(NBTItem item, LivingEntity target) {
        if (getDamage().hasType(DamageType.WEAPON))
            applyElementalEffects(item, target);
        return this;
    }

    /**
     * Applies MMOItem specific on-hit effects like elemental damage.
     *
     * @param item   The item being used
     * @param target The entity target
     * @return The unedited attack result
     */
    @SuppressWarnings("UnusedReturnValue")
    public ItemAttackMetadata applyElementalEffects(NBTItem item, LivingEntity target) {
        double damageModifier = new ElementalAttack(this, item, getDamage().getDamage(), target).getDamageModifier();
        getDamage().add(damageModifier);
        return this;
    }
}
