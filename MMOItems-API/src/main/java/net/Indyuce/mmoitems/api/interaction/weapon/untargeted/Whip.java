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
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class Whip extends UntargetedWeapon {
    public Whip(Player player, NBTItem item) {
        super(player, item, UntargetedWeaponType.LEFT_CLICK);
    }

    @Override
    public boolean canAttack(EquipmentSlot slot) {
        return true;
    }

    @Override
    public void applyAttackEffect(PlayerMetadata stats, EquipmentSlot slot) {

        double attackDamage = requireNonZero(stats.getStat("ATTACK_DAMAGE"), 7);
        double range = requireNonZero(getNBTItem().getStat(ItemStats.RANGE.getId()), MMOItems.plugin.getConfig().getDouble("default.range"));

        RayTrace trace = new RayTrace(getPlayer(), slot, range, entity -> UtilityMethods.canTarget(stats.getPlayer(), entity, InteractionType.OFFENSE_ACTION));
        if (trace.hasHit())
            stats.attack(trace.getHit(), attackDamage, DamageType.WEAPON, DamageType.PROJECTILE, DamageType.PHYSICAL);
        trace.draw(.5, tick -> tick.getWorld().spawnParticle(Particle.CRIT, tick, 0, .1, .1, .1, 0));
        getPlayer().getWorld().playSound(getPlayer().getLocation(), VersionSound.ENTITY_FIREWORK_ROCKET_BLAST.toSound(), 1, 2);
    }
}
