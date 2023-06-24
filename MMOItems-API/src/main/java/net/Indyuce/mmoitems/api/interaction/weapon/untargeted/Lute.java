package net.Indyuce.mmoitems.api.interaction.weapon.untargeted;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.comp.interaction.InteractionType;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.version.VersionSound;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.util.SoundReader;
import net.Indyuce.mmoitems.stat.LuteAttackEffectStat.LuteAttackEffect;
import net.Indyuce.mmoitems.stat.data.ProjectileParticlesData;
import net.Indyuce.mmoitems.util.MMOUtils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class Lute extends UntargetedWeapon {
    public Lute(Player player, NBTItem item) {
        super(player, item, UntargetedWeaponType.RIGHT_CLICK);
    }

    @Override
    public boolean canAttack(EquipmentSlot slot) {
        return true;
    }

    @Override
    public void applyAttackEffect(PlayerMetadata stats, EquipmentSlot slot) {
        final double attackDamage = requireNonZero(stats.getStat("ATTACK_DAMAGE"), 7);
        final double range = requireNonZero(stats.getStat("RANGE"), MMOItems.plugin.getConfig().getDouble("default.range"));
        final Vector weight = new Vector(0, -.003 * stats.getStat("NOTE_WEIGHT"), 0);

        final @Nullable LuteAttackEffect effect = LuteAttackEffect.get(getNBTItem());
        @Deprecated
        final SoundReader sound = new SoundReader(getNBTItem().getString("MMOITEMS_LUTE_ATTACK_SOUND"), VersionSound.BLOCK_NOTE_BLOCK_BELL.toSound());
        final @NotNull ProjectileParticlesData projParticle = getNBTItem().hasTag("MMOITEMS_PROJECTILE_PARTICLES") ?
                new ProjectileParticlesData(getNBTItem().getString("MMOITEMS_PROJECTILE_PARTICLES")) : ProjectileParticlesData.DEFAULT;

        // Custom attack handler
        if (effect != null) {
            effect.getAttack().handle(stats, attackDamage, getNBTItem(), range, weight, sound, projParticle);
            return;
        }

        // Basic lute attack
        new BukkitRunnable() {
            final Vector vec = getPlayer().getEyeLocation().getDirection().multiply(.4);
            final Location loc = getPlayer().getEyeLocation();
            int ti = 0;

            public void run() {
                if (ti++ > range)
                    cancel();

                // Display custom particle
                projParticle.shootParticle(loc);

                // Play the sound
                sound.play(loc, 2, (float) (.5 + (double) ti / range));

                // Damage entities
                List<Entity> entities = MMOUtils.getNearbyChunkEntities(loc);
                for (int j = 0; j < 3; j++) {
                    loc.add(vec.add(weight));
                    if (loc.getBlock().getType().isSolid()) {
                        cancel();
                        break;
                    }

                    for (Entity target : entities)
                        if (UtilityMethods.canTarget(getPlayer(), loc, target, InteractionType.OFFENSE_ACTION)) {
                            stats.attack((LivingEntity) target, attackDamage, DamageType.WEAPON, DamageType.MAGIC, DamageType.PROJECTILE);
                            cancel();
                            return;
                        }
                }
            }
        }.runTaskTimer(MMOItems.plugin, 0, 1);
    }
}
