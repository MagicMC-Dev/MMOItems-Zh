package net.Indyuce.mmoitems.ability;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.Indyuce.mmoitems.api.player.damage.AttackResult;
import net.Indyuce.mmoitems.api.player.damage.AttackResult.DamageType;
import net.Indyuce.mmoitems.api.util.NoInteractItemEntity;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.Indyuce.mmoitems.version.VersionMaterial;
import net.Indyuce.mmoitems.version.VersionSound;

public class Present_Throw extends Ability {
	private ItemStack present = VersionMaterial.PLAYER_HEAD.toItem();

	public Present_Throw() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("damage", 6);
		addModifier("radius", 4);
		addModifier("force", 1);
		addModifier("cooldown", 10);
		addModifier("mana", 0);
		addModifier("stamina", 0);

		ItemMeta presentMeta = present.getItemMeta();
		GameProfile profile = new GameProfile(UUID.randomUUID(), null);
		byte[] encodedData = Base64Coder.encodeLines(String.format("{textures:{SKIN:{url:\"%s\"}}}", "http://textures.minecraft.net/texture/47e55fcc809a2ac1861da2a67f7f31bd7237887d162eca1eda526a7512a64910").getBytes()).getBytes();
		profile.getProperties().put("textures", new Property("textures", new String(encodedData)));

		try {
			Field profileField = presentMeta.getClass().getDeclaredField("profile");
			profileField.setAccessible(true);
			profileField.set(presentMeta, profile);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			MMOItems.plugin.getLogger().log(Level.WARNING, "Could not load the skull texture for the Present Throw item ability.");
		}

		present.setItemMeta(presentMeta);
	}

	@Override
	public void whenCast(TemporaryStats stats, LivingEntity target, AbilityData data, AttackResult result) {
		double damage = data.getModifier("damage");
		double radiusSquared = Math.pow(data.getModifier("radius"), 2);

		final NoInteractItemEntity item = new NoInteractItemEntity(stats.getPlayer().getLocation().add(0, 1.2, 0), present);
		item.getEntity().setVelocity(stats.getPlayer().getEyeLocation().getDirection().multiply(1.5 * data.getModifier("force")));

		/*
		 * when items are moving through the air, they loose a percent of their
		 * velocity proportionally to their coordinates in each axis. this means
		 * that if the trajectory is not affected, the ratio of x/y will always
		 * be the same. check for any change of that ratio to check for a
		 * trajectory change
		 */
		final double trajRatio = item.getEntity().getVelocity().getX() / item.getEntity().getVelocity().getZ();
		stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), Sound.ENTITY_SNOWBALL_THROW, 1, 0);
		new BukkitRunnable() {
			double ti = 0;

			public void run() {
				if (ti++ > 70 || item.getEntity().isDead()) {
					item.close();
					cancel();
				}

				double currentTrajRatio = item.getEntity().getVelocity().getX() / item.getEntity().getVelocity().getZ();
				item.getEntity().getWorld().spawnParticle(Particle.SPELL_INSTANT, item.getEntity().getLocation().add(0, .1, 0), 0);
				if (item.getEntity().isOnGround() || Math.abs(trajRatio - currentTrajRatio) > .1) {
					item.getEntity().getWorld().spawnParticle(Particle.FIREWORKS_SPARK, item.getEntity().getLocation().add(0, .1, 0), 128, 0, 0, 0, .25);
					item.getEntity().getWorld().playSound(item.getEntity().getLocation(), VersionSound.ENTITY_FIREWORK_ROCKET_TWINKLE.toSound(), 2, 1.5f);
					for (Entity entity : MMOUtils.getNearbyChunkEntities(item.getEntity().getLocation()))
						if (entity.getLocation().distanceSquared(item.getEntity().getLocation()) < radiusSquared && MMOUtils.canDamage(stats.getPlayer(), entity))
							new AttackResult(damage, DamageType.SKILL, DamageType.MAGICAL, DamageType.PROJECTILE).applyEffectsAndDamage(stats, null, (LivingEntity) entity);
					item.close();
					cancel();
				}
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}
