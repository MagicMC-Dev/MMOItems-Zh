package net.Indyuce.mmoitems.ability.list.location;

import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.version.VersionSound;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.ability.LocationAbility;
import net.Indyuce.mmoitems.ability.metadata.LocationAbilityMetadata;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class Freezing_Curse extends LocationAbility {
	public Freezing_Curse() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("cooldown", 7);
		addModifier("duration", 3);
		addModifier("damage", 3);
		addModifier("radius", 3);
		addModifier("amplifier", 1);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public void whenCast(ItemAttackMetadata attack, LocationAbilityMetadata ability) {
		Location loc = ability.getTarget();

		new BukkitRunnable() {
			final double rads = Math.toRadians(attack.getDamager().getEyeLocation().getYaw() - 90);
			double ti = rads;
			int j = 0;

			public void run() {

				if (j++ % 2 == 0)
					loc.getWorld().playSound(loc, VersionSound.BLOCK_NOTE_BLOCK_PLING.toSound(), 2, (float) (.5 + ((ti - rads) / (Math.PI * 2) * 1.5)));
				for (int j = 0; j < 2; j++) {
					ti += Math.PI / 32;
					loc.getWorld().spawnParticle(Particle.SPELL_INSTANT, loc.clone().add(Math.cos(ti) * 3, .1, Math.sin(ti) * 3), 0);
				}

				if (ti > Math.PI * 2 + rads) {
					loc.getWorld().playSound(loc, Sound.BLOCK_GLASS_BREAK, 3, .5f);

					for (double j = 0; j < Math.PI * 2; j += Math.PI / 32)
						loc.getWorld().spawnParticle(Particle.CLOUD, loc.clone().add(Math.cos(j) * 3, .1, Math.sin(j) * 3), 0);

					double radius = ability.getModifier("radius");
					double amplifier = ability.getModifier("amplifier");
					double duration = ability.getModifier("duration");
					double damage = ability.getModifier("damage");
					for (Entity entity : MMOUtils.getNearbyChunkEntities(loc))
						if (entity.getLocation().distanceSquared(loc) < radius * radius && MMOUtils.canDamage(attack.getDamager(), entity)) {
							new AttackMetadata(new DamageMetadata(damage, DamageType.SKILL, DamageType.MAGIC), attack.getStats()).damage((LivingEntity) entity);
							((LivingEntity) entity).removePotionEffect(PotionEffectType.SLOW);
							((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (duration * 20), (int) amplifier));
						}
					cancel();
				}
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}
