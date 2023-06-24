package net.Indyuce.mmoitems.api.interaction.weapon.untargeted;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.comp.interaction.InteractionType;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.util.RayTrace;
import io.lumine.mythic.lib.version.VersionSound;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.player.PlayerData.CooldownType;
import net.Indyuce.mmoitems.stat.StaffSpiritStat.StaffSpirit;
import org.bukkit.EntityEffect;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Staff extends UntargetedWeapon {
    public Staff(Player player, NBTItem item) {
        super(player, item, UntargetedWeaponType.LEFT_CLICK);
    }

    @Override
    public boolean canAttack(EquipmentSlot slot) {
        return true;
    }

    @Override
    public void applyAttackEffect(PlayerMetadata stats, EquipmentSlot slot) {

        double attackDamage = requireNonZero(stats.getStat("ATTACK_DAMAGE"), 1);
        double range = requireNonZero(stats.getStat("RANGE"), MMOItems.plugin.getConfig().getDouble("default.range"));

        StaffSpirit spirit = StaffSpirit.get(getNBTItem());
        if (spirit != null) {
            spirit.getAttack().handle(stats, attackDamage, getNBTItem(), slot, range);
            return;
        }

        RayTrace trace = new RayTrace(stats.getPlayer(), slot, range, entity -> UtilityMethods.canTarget(stats.getPlayer(), entity, InteractionType.OFFENSE_ACTION));
        if (trace.hasHit())
            stats.attack(trace.getHit(), attackDamage, DamageType.WEAPON, DamageType.MAGIC, DamageType.PROJECTILE);
        trace.draw(.5, tick -> tick.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, tick, 0, .1, .1, .1, 0));
        getPlayer().getWorld().playSound(getPlayer().getLocation(), VersionSound.ENTITY_FIREWORK_ROCKET_TWINKLE.toSound(), 2, 2);

    }

    public void specialAttack(LivingEntity target) {
        if (!MMOItems.plugin.getConfig().getBoolean("item-ability.staff.enabled"))
            return;

        if (!checkWeaponCosts(CooldownType.SPECIAL_ATTACK))
            return;

        applyWeaponCosts(MMOItems.plugin.getConfig().getDouble("item-ability.staff.cooldown"), CooldownType.SPECIAL_ATTACK);
        double power = MMOItems.plugin.getConfig().getDouble("item-ability.staff.power");

        try {
            Vector vec = target.getLocation().toVector().subtract(getPlayer().getLocation().toVector()).setY(0).normalize().multiply(1.75 * power).setY(.65 * power);
            target.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, target.getLocation().add(0, 1, 0), 0);
            target.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, target.getLocation().add(0, 1, 0), 16, 0, 0, 0, .1);
            target.setVelocity(vec);
            target.playEffect(EntityEffect.HURT);
            target.getWorld().playSound(target.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 2);
        } catch (IllegalArgumentException ignored) {
        }
    }
}