package net.Indyuce.mmoitems.api;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.comp.interaction.InteractionType;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.version.VersionSound;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.util.MMOUtils;
import net.Indyuce.mmoitems.api.interaction.weapon.Weapon;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.PlayerData.CooldownType;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public enum TypeSet {

    /**
     * Slashing weapons deal damage in a cone behind the player's initial
     * target, which makes it a deadly AoE weapon for warriors
     */
    SLASHING((attack, damager, target, weapon) -> {
        if (!MMOItems.plugin.getConfig().getBoolean("item-ability.slashing.enabled") || damager.isOnCooldown(CooldownType.SET_TYPE_ATTACK))
            return;

        damager.applyCooldown(CooldownType.SET_TYPE_ATTACK, MMOItems.plugin.getConfig().getDouble("item-ability.slashing.cooldown"));
        Location loc = attack.getPlayer().getLocation().clone().add(0, 1.3, 0);

        final double a1 = (loc.getYaw() + 90) / 180 * Math.PI, p = -loc.getPitch() / 180 * Math.PI;
        for (double r = 1; r < 5; r += .3)
            for (double a = -Math.PI / 6; a < Math.PI / 6; a += Math.PI / 8 / r)
                loc.getWorld().spawnParticle(Particle.CRIT, loc.clone().add(Math.cos(a + a1) * r, Math.sin(p) * r, Math.sin(a + a1) * r), 0);

        for (Entity entity : MMOUtils.getNearbyChunkEntities(loc))
            if (entity.getLocation().distanceSquared(loc) < 40
                    && attack.getPlayer().getEyeLocation().getDirection()
                    .angle(entity.getLocation().subtract(attack.getPlayer().getLocation()).toVector()) < Math.PI / 3
                    && UtilityMethods.canTarget(attack.getPlayer(), entity, InteractionType.OFFENSE_ACTION) && !entity.equals(target))
                attack.attack((LivingEntity) entity, attack.getDamage().getDamage() * .4, DamageType.WEAPON, DamageType.PHYSICAL);
    }),

    /**
     * Piercing weapons deal damage in a line behind the initial target, which
     * is harder to land than a slashing weapon but the AoE damage ratio is
     * increased which makes it a perfect 'double or nothing' weapon for
     * assassins
     */
    PIERCING((attack, damager, target, weapon) -> {
        if (!MMOItems.plugin.getConfig().getBoolean("item-ability.piercing.enabled") || damager.isOnCooldown(CooldownType.SET_TYPE_ATTACK))
            return;

        damager.applyCooldown(CooldownType.SET_TYPE_ATTACK, MMOItems.plugin.getConfig().getDouble("item-ability.piercing.cooldown"));
        Location loc = attack.getPlayer().getLocation().clone().add(0, 1.3, 0);

        final double a1 = (loc.getYaw() + 90) / 180 * Math.PI, p = -loc.getPitch() / 180 * Math.PI;
        for (double r = 1; r < 5; r += .3)
            for (double a = -Math.PI / 12; a < Math.PI / 12; a += Math.PI / 16 / r)
                loc.getWorld().spawnParticle(Particle.CRIT, loc.clone().add(Math.cos(a + a1) * r, Math.sin(p) * r, Math.sin(a + a1) * r), 0);

        for (Entity entity : MMOUtils.getNearbyChunkEntities(loc))
            if (!entity.equals(target) && entity.getLocation().distanceSquared(attack.getPlayer().getLocation()) < 40
                    && attack.getPlayer().getEyeLocation().getDirection()
                    .angle(entity.getLocation().toVector().subtract(attack.getPlayer().getLocation().toVector())) < Math.PI / 12
                    && UtilityMethods.canTarget(attack.getPlayer(), entity, InteractionType.OFFENSE_ACTION))
                attack.attack((LivingEntity) entity, attack.getDamage().getDamage() * .6, DamageType.WEAPON, DamageType.PHYSICAL);
    }),

    /**
     * Blunt weapons are like 1.9 sweep attacks. They damage
     * all enemies nearby and apply a slight knockback
     */
    BLUNT((attack, damager, target, weapon) -> {
        final Random random = new Random();
        float pitchRange = 0.7f + random.nextFloat() * (0.9f - 0.7f);

        if (MMOItems.plugin.getConfig().getBoolean("item-ability.blunt.aoe.enabled")
                && !damager.isOnCooldown(CooldownType.SPECIAL_ATTACK)) {

            damager.applyCooldown(CooldownType.SPECIAL_ATTACK, MMOItems.plugin.getConfig().getDouble("item-ability.blunt.aoe.cooldown"));
            target.getWorld().playSound(target.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.6f, pitchRange);
            target.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, target.getLocation().add(0, 1, 0), 0);
            double bluntPower = attack.getStat("BLUNT_POWER");
            if (bluntPower > 0) {
                double bluntRating = weapon.requireNonZero(attack.getStat("BLUNT_RATING"),
                        MMOItems.plugin.getConfig().getDouble("default.blunt-rating")) / 100;
                for (Entity entity : target.getNearbyEntities(bluntPower, bluntPower, bluntPower))
                    if (UtilityMethods.canTarget(attack.getPlayer(), entity, InteractionType.OFFENSE_ACTION) && !entity.equals(target))
                        attack.attack((LivingEntity) entity, attack.getDamage().getDamage() * bluntRating, DamageType.WEAPON, DamageType.PHYSICAL);
            }
        }

        if (MMOItems.plugin.getConfig().getBoolean("item-ability.blunt.stun.enabled")
                && !damager.isOnCooldown(CooldownType.SPECIAL_ATTACK)
                && random.nextDouble() < MMOItems.plugin.getConfig().getDouble("item-ability.blunt.stun.chance") / 100) {

            damager.applyCooldown(CooldownType.SPECIAL_ATTACK, MMOItems.plugin.getConfig().getDouble("item-ability.blunt.stun.cooldown"));
            target.getWorld().playSound(target.getLocation(), VersionSound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR.toSound(), 1, 2);
            target.removePotionEffect(PotionEffectType.SLOW);
            target.removePotionEffect(PotionEffectType.BLINDNESS);
            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 0));
            target.addPotionEffect(
                    new PotionEffect(PotionEffectType.SLOW, (int) (30 * MMOItems.plugin.getConfig().getDouble("item-ability.blunt.stun.power")), 1));
            Location loc = target.getLocation();
            loc.setYaw((float) (loc.getYaw() + 2 * (random.nextDouble() - .5) * 90));
            loc.setPitch((float) (loc.getPitch() + 2 * (random.nextDouble() - .5) * 30));
        }
    }),

    /**
     * Ranged attacks based weapons. when the player is too squishy to fight in
     * the middle of the battle-field, these weapons allow him to take some
     * distance and still deal some good damage
     */
    RANGE,

    /**
     * Hand/Mainhand/Offhand catalysts
     */
    CATALYST,

    /**
     * Any other item type, like armor, consumables, etc. They all have their
     * very specific passive depending on their item type
     */
    EXTRA;

    private final SetAttackHandler attackHandler;
    private final String name;

    private TypeSet() {
        this(null);
    }

    private TypeSet(SetAttackHandler attackHandler) {
        this.attackHandler = attackHandler;

        this.name = MMOUtils.caseOnWords(name().toLowerCase());
    }

    public boolean hasAttackEffect() {
        return attackHandler != null;
    }

    public void applyAttackEffect(AttackMetadata attackMeta, PlayerData damager, LivingEntity target, Weapon weapon) {
        attackHandler.apply(attackMeta, damager, target, weapon);
    }

    public String getName() {
        return name;
    }

    @FunctionalInterface
    interface SetAttackHandler {
        void apply(AttackMetadata attack, PlayerData damager, LivingEntity target, Weapon weapon);
    }
}
