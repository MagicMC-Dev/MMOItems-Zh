package net.Indyuce.mmoitems.api.interaction.projectile;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.player.PlayerMetadata;
import net.Indyuce.mmoitems.stat.data.PotionEffectData;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffectType;

/**
 * Since MMOItems 6.7.5 vanilla bows and custom bows are
 * not treated the same way:
 * - vanilla bows see NO changes in their damage computations
 * - custom bows override
 */
public class ProjectileData {
    private final NBTItem sourceItem;
    private final PlayerMetadata shooter;
    private final double damageMultiplier;

    @Deprecated
    public ProjectileData(PlayerMetadata shooter, NBTItem sourceItem, boolean customWeapon, double damageMultiplier) {
        this(shooter, sourceItem, damageMultiplier);
    }

    public ProjectileData(PlayerMetadata shooter, NBTItem sourceItem, double damageMultiplier) {
        this.shooter = shooter;
        this.sourceItem = sourceItem;
        this.damageMultiplier = damageMultiplier;
    }

    public NBTItem getSourceItem() {
        return sourceItem;
    }

    public PlayerMetadata getShooter() {
        return shooter;
    }

    public double getDamageMultiplier() {
        return damageMultiplier;
    }

    /**
     * Used to check if that projectile data is linked to
     * a projectile that want sent using a MMOItems bow.
     * <p>
     * If so, it needs to apply on-hit effects like
     * elemental damage or on-hit potion effects
     */
    @Deprecated
    public boolean isCustomWeapon() {
        return true;
    }

    /**
     * Will throw an error if it's not a custom bow
     *
     * @return Damage of custom bow
     */
    public double getDamage() {
        Validate.isTrue(isCustomWeapon(), "Not a custom bow");
        return shooter.getStat("ATTACK_DAMAGE") * damageMultiplier;
    }

    /**
     * @see {@link #getDamage()}
     */
    @Deprecated
    public void setDamage(double damage) {
        Validate.isTrue(isCustomWeapon(), "Not a custom bow");
        shooter.setStat("ATTACK_DAMAGE", damage);
    }

    public void applyPotionEffects(LivingEntity target) {
        if (sourceItem.hasTag("MMOITEMS_ARROW_POTION_EFFECTS"))
            for (ArrowPotionEffectArrayItem entry : MythicLib.plugin.getJson().parse(sourceItem.getString("MMOITEMS_ARROW_POTION_EFFECTS"), ArrowPotionEffectArrayItem[].class))
                target.addPotionEffect(new PotionEffectData(PotionEffectType.getByName(entry.type), entry.duration, entry.level).toEffect());
    }
}
