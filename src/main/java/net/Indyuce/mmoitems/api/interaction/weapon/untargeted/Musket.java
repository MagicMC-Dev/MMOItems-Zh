package net.Indyuce.mmoitems.api.interaction.weapon.untargeted;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.comp.target.InteractionType;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.util.RayTrace;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Musket extends UntargetedWeapon {
    public Musket(Player player, NBTItem item) {
        super(player, item, UntargetedWeaponType.RIGHT_CLICK);
    }

    @Override
    public boolean canAttack(EquipmentSlot slot) {
        return true;
    }

    @Override
    public void applyAttackEffect(PlayerMetadata stats, EquipmentSlot slot) {
        double attackDamage = stats.getStat("ATTACK_DAMAGE");
        double range = getValue(getNBTItem().getStat(ItemStats.RANGE.getId()), MMOItems.plugin.getConfig().getDouble("default.range"));
        double recoil = getValue(getNBTItem().getStat(ItemStats.RECOIL.getId()), MMOItems.plugin.getConfig().getDouble("default.recoil"));

        // knockback
        double knockback = getNBTItem().getStat(ItemStats.KNOCKBACK.getId());
        if (knockback > 0)
            getPlayer().setVelocity(getPlayer().getVelocity()
                    .add(getPlayer().getEyeLocation().getDirection().setY(0).normalize().multiply(-1 * knockback).setY(-.2)));

        double a = Math.toRadians(getPlayer().getEyeLocation().getYaw() + 90 + 45 * (slot == EquipmentSlot.MAIN_HAND ? 1 : -1));
        Location loc = getPlayer().getLocation().add(Math.cos(a) * .5, 1.5, Math.sin(a) * .5);

        loc.setPitch((float) (loc.getPitch() + (RANDOM.nextDouble() - .5) * 2 * recoil));
        loc.setYaw((float) (loc.getYaw() + (RANDOM.nextDouble() - .5) * 2 * recoil));
        Vector vec = loc.getDirection();

        RayTrace trace = new RayTrace(loc, vec, range, entity -> MMOUtils.canTarget(stats.getPlayer(), entity, InteractionType.OFFENSE_ACTION));
        if (trace.hasHit()) {
            ItemAttackMetadata attackMeta = new ItemAttackMetadata(new DamageMetadata(attackDamage, DamageType.WEAPON, DamageType.PROJECTILE, DamageType.PHYSICAL), stats);
            attackMeta.applyEffectsAndDamage(getNBTItem(), trace.getHit());
        }

        trace.draw(.5, Color.BLACK);
        getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 2, 2);
    }
}
