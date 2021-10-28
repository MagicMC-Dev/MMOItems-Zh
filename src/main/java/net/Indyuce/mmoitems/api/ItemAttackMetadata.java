package net.Indyuce.mmoitems.api;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.stat.StatMap;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import net.Indyuce.mmoitems.ability.Ability;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.bukkit.entity.LivingEntity;

/**
 * The attack metadata used here in MMOItems. It
 * extends the default attack metadata from MythicLib
 * to benefit from all its methods
 */
public class ItemAttackMetadata extends AttackMetadata {
    public ItemAttackMetadata(DamageMetadata damage, StatMap.CachedStatMap damager) {
        super(damage, damager);
    }

    public ItemAttackMetadata(AttackMetadata attackMeta) {
        super(attackMeta.getDamage(), attackMeta.getStats());
    }

    public PlayerData getPlayerData() {
        return PlayerData.get(getDamager().getUniqueId());
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
        if (getDamage().hasType(DamageType.WEAPON)) {
            applyElementalEffects(item, target);
            applyOnHitEffects(target);
        }
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
        new ElementalAttack(item, this, target).apply();
        return this;
    }

    /**
     * This method is called when a player uses ANY weapon, vanilla or custom.
     * It does not take into input any weapon as it just applies non weapon
     * specific on-hit effects
     *
     * @param target The entity target
     * @return The unedited attack result
     */
    public ItemAttackMetadata applyOnHitEffects(LivingEntity target) {
        getPlayerData().castAbilities(this, target, Ability.CastingMode.ON_HIT);
        return this;
    }
}
