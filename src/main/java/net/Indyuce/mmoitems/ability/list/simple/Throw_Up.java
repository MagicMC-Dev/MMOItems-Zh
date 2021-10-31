package net.Indyuce.mmoitems.ability.list.simple;

import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.ability.SimpleAbility;
import net.Indyuce.mmoitems.ability.metadata.SimpleAbilityMetadata;
import io.lumine.mythic.lib.damage.AttackMetadata;
import net.Indyuce.mmoitems.api.util.NoClipItem;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class Throw_Up extends SimpleAbility implements Listener {
    public Throw_Up() {
        super();

        addModifier("duration", 2.5);
        addModifier("damage", 2);
        addModifier("cooldown", 10);
        addModifier("mana", 0);
        addModifier("stamina", 0);
    }

    @Override
    public void whenCast(AttackMetadata attack, SimpleAbilityMetadata ability) {
        double duration = ability.getModifier("duration") * 10;
        double dps = ability.getModifier("damage") / 2;

        new BukkitRunnable() {
            int j = 0;

            public void run() {
                j++;
                if (j > duration)
                    cancel();

                Location loc = attack.getPlayer().getEyeLocation();
                loc.setPitch((float) (loc.getPitch() + (random.nextDouble() - .5) * 30));
                loc.setYaw((float) (loc.getYaw() + (random.nextDouble() - .5) * 30));

                if (j % 5 == 0)
                    for (Entity entity : MMOUtils.getNearbyChunkEntities(loc))
                        if (entity.getLocation().distanceSquared(loc) < 40 && attack.getPlayer().getEyeLocation().getDirection().angle(entity.getLocation().toVector().subtract(attack.getPlayer().getLocation().toVector())) < Math.PI / 6 && MMOUtils.canTarget(attack.getPlayer(), entity))
                            new AttackMetadata(new DamageMetadata(dps, DamageType.SKILL, DamageType.PHYSICAL, DamageType.PROJECTILE), attack.getStats()).damage((LivingEntity) entity);

                loc.getWorld().playSound(loc, Sound.ENTITY_ZOMBIE_HURT, 1, 1);

                NoClipItem item = new NoClipItem(attack.getPlayer().getLocation().add(0, 1.2, 0), new ItemStack(Material.ROTTEN_FLESH));
                Bukkit.getScheduler().scheduleSyncDelayedTask(MMOItems.plugin, item::close, 40);
                item.getEntity().setVelocity(loc.getDirection().multiply(.8));
                attack.getPlayer().getWorld().spawnParticle(Particle.SMOKE_LARGE, attack.getPlayer().getLocation().add(0, 1.2, 0), 0, loc.getDirection().getX(), loc.getDirection().getY(), loc.getDirection().getZ(), 1);
            }
		}.runTaskTimer(MMOItems.plugin, 0, 2);
	}
}
