package net.Indyuce.mmoitems.ability.list.simple;

import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.version.VersionSound;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.ability.SimpleAbility;
import net.Indyuce.mmoitems.ability.metadata.SimpleAbilityMetadata;
import io.lumine.mythic.lib.damage.AttackMetadata;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Firefly extends SimpleAbility {
	public Firefly() {
		super();

		addModifier("damage", 6);
		addModifier("duration", 2.5);
		addModifier("knockback", 1);
		addModifier("cooldown", 10);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public void whenCast(AttackMetadata attack, SimpleAbilityMetadata ability) {
		double duration = ability.getModifier("duration") * 20;

		new BukkitRunnable() {
			int j = 0;

			public void run() {
				if (j++ > duration)
					cancel();

				if (attack.getPlayer().getLocation().getBlock().getType() == Material.WATER) {
					attack.getPlayer().setVelocity(attack.getPlayer().getVelocity().multiply(3).setY(1.8));
					attack.getPlayer().getWorld().playSound(attack.getPlayer().getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1, .5f);
					attack.getPlayer().getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, attack.getPlayer().getLocation().add(0, 1, 0), 32, 0, 0, 0, .2);
					attack.getPlayer().getWorld().spawnParticle(Particle.CLOUD, attack.getPlayer().getLocation().add(0, 1, 0), 32, 0, 0, 0, .2);
					cancel();
					return;
				}

				for (Entity entity : attack.getPlayer().getNearbyEntities(1, 1, 1))
					if (MMOUtils.canTarget(attack.getPlayer(), entity)) {
						double damage = ability.getModifier("damage");
						double knockback = ability.getModifier("knockback");

						attack.getPlayer().getWorld().playSound(attack.getPlayer().getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1, .5f);
						attack.getPlayer().getWorld().spawnParticle(Particle.LAVA, attack.getPlayer().getLocation().add(0, 1, 0), 32);
						attack.getPlayer().getWorld().spawnParticle(Particle.SMOKE_LARGE, attack.getPlayer().getLocation().add(0, 1, 0), 24, 0, 0, 0, .3);
						attack.getPlayer().getWorld().spawnParticle(Particle.FLAME, attack.getPlayer().getLocation().add(0, 1, 0), 24, 0, 0, 0, .3);
						entity.setVelocity(attack.getPlayer().getVelocity().setY(0.3).multiply(1.7 * knockback));
						attack.getPlayer().setVelocity(attack.getPlayer().getEyeLocation().getDirection().multiply(-3).setY(.5));
						new AttackMetadata(new DamageMetadata(damage, DamageType.SKILL, DamageType.MAGIC), attack.getStats()).damage((LivingEntity) entity);
						cancel();
						return;
					}

				Location loc = attack.getPlayer().getLocation().add(0, 1, 0);
				for (double a = 0; a < Math.PI * 2; a += Math.PI / 9) {
					Vector vec = new Vector(.6 * Math.cos(a), .6 * Math.sin(a), 0);
					vec = MMOUtils.rotateFunc(vec, loc);
					loc.add(vec);
					attack.getPlayer().getWorld().spawnParticle(Particle.SMOKE_NORMAL, loc, 0);
					if (random.nextDouble() < .3)
						attack.getPlayer().getWorld().spawnParticle(Particle.FLAME, loc, 0);
					loc.add(vec.multiply(-1));
				}

				attack.getPlayer().setVelocity(attack.getPlayer().getEyeLocation().getDirection());
				attack.getPlayer().getWorld().playSound(attack.getPlayer().getLocation(), VersionSound.ENTITY_FIREWORK_ROCKET_BLAST.toSound(), 1, 1);
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}
