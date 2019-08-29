package net.Indyuce.mmoitems.api;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Consumer;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.AttackResult.DamageType;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.Indyuce.mmoitems.listener.ElementListener;
import net.Indyuce.mmoitems.version.VersionMaterial;
import net.Indyuce.mmoitems.version.VersionSound;

public enum Element {
	FIRE(Material.BLAZE_POWDER, "Fire", ChatColor.DARK_RED, new ElementParticle(Particle.FLAME, .05f, 8), new ElementHandler() {
		public void elementAttack(TemporaryStats stats, AttackResult result, LivingEntity target, double attack, double absolute) {
			target.getWorld().spawnParticle(Particle.LAVA, target.getLocation().add(0, target.getHeight() / 2, 0), 14);
			target.getWorld().playSound(target.getLocation(), Sound.ENTITY_BLAZE_HURT, 2, .8f);
			target.setFireTicks((int) (attack * 2));
			result.addDamage(absolute);
		}
	}),

	ICE(VersionMaterial.SNOWBALL.toMaterial(), "Ice", ChatColor.AQUA, new ElementParticle(Particle.BLOCK_CRACK, .07f, 16, Material.ICE), new ElementHandler() {
		public void elementAttack(TemporaryStats stats, AttackResult result, LivingEntity target, double attack, double absolute) {
			new BukkitRunnable() {
				double y = 0;
				Location loc = target.getLocation();

				public void run() {
					for (int j = 0; j < 3; j++) {
						if ((y += .07) >= 3)
							cancel();
						for (double k = 0; k < Math.PI * 2; k += Math.PI * 2 / 3)
							MMOItems.plugin.getVersion().getVersionWrapper().spawnParticle(Particle.REDSTONE, loc.clone().add(Math.cos(y * Math.PI + k) * (3 - y) / 2.5, y / 1.1, Math.sin(y * Math.PI + k) * (3 - y) / 2.5), Color.WHITE);
					}
				}
			}.runTaskTimer(MMOItems.plugin, 0, 1);
			target.getWorld().playSound(target.getLocation(), Sound.BLOCK_GLASS_BREAK, 2, 0);
			target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (attack * 1.5), 5));
			result.addDamage(absolute);
		}
	}),

	WIND(Material.FEATHER, "Wind", ChatColor.GRAY, new ElementParticle(Particle.EXPLOSION_NORMAL, .06f, 8), new ElementHandler() {
		public void elementAttack(TemporaryStats stats, AttackResult result, LivingEntity target, double attack, double absolute) {
			target.getWorld().playSound(target.getLocation(), VersionSound.ENTITY_ENDER_DRAGON_GROWL.toSound(), 2, 2f);
			Vector vec = target.getLocation().subtract(stats.getPlayer().getLocation()).toVector().normalize().multiply(1.7).setY(.5);
			target.setVelocity(vec);
			for (Entity entity : target.getNearbyEntities(3, 1, 3))
				if (MMOUtils.canDamage(stats.getPlayer(), entity)) {
					entity.playEffect(EntityEffect.HURT);
					entity.setVelocity(vec);
				}
			result.addDamage(absolute);
			for (double k = 0; k < Math.PI * 2; k += Math.PI / 16)
				target.getWorld().spawnParticle(Particle.CLOUD, target.getLocation().add(0, target.getHeight() / 2, 0), 0, Math.cos(k), .01, Math.sin(k), .15);
		}
	}),

	EARTH(VersionMaterial.OAK_SAPLING.toMaterial(), "Earth", ChatColor.GREEN, new ElementParticle(Particle.BLOCK_CRACK, .05f, 24, Material.DIRT), new ElementHandler() {
		public void elementAttack(TemporaryStats stats, AttackResult result, LivingEntity target, double attack, double absolute) {
			target.getWorld().playSound(target.getLocation(), Sound.BLOCK_GRASS_BREAK, 2, 0);
			MMOItems.plugin.getVersion().getVersionWrapper().spawnParticle(Particle.BLOCK_CRACK, target.getLocation().add(0, .1, 0), 64, 1, 0, 1, 0, Material.DIRT);
			result.addDamage(absolute);

			target.setVelocity(new Vector(0, 1, 0));
			for (Entity entity : target.getNearbyEntities(3, 1, 3))
				if (MMOUtils.canDamage(stats.getPlayer(), entity))
					entity.setVelocity(new Vector(0, 1, 0));
		}
	}),

	THUNDER(VersionMaterial.GUNPOWDER.toMaterial(), "Thunder", ChatColor.YELLOW, new ElementParticle(Particle.FIREWORKS_SPARK, .05f, 8), new ElementHandler() {
		public void elementAttack(TemporaryStats stats, AttackResult result, LivingEntity target, double attack, double absolute) {
			target.getWorld().playSound(target.getLocation(), VersionSound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST.toSound(), 2, 0);
			for (Entity entity : target.getNearbyEntities(3, 2, 3))
				if (MMOUtils.canDamage(stats.getPlayer(), entity))
					new AttackResult(result.getDamage() * attack / 100, DamageType.WEAPON).damage(stats, (LivingEntity) entity);

			result.addDamage(absolute);
			for (double k = 0; k < Math.PI * 2; k += Math.PI / 16)
				target.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, target.getLocation().add(0, target.getHeight() / 2, 0), 0, Math.cos(k), .01, Math.sin(k), .18);
		}
	}),

	WATER(VersionMaterial.LILY_PAD.toMaterial(), "Water", ChatColor.BLUE, new ElementParticle(Particle.BLOCK_CRACK, .07f, 32, Material.WATER), new ElementHandler() {
		public void elementAttack(TemporaryStats stats, AttackResult result, LivingEntity target, double attack, double absolute) {
			ElementListener.weaken(target);
			new BukkitRunnable() {
				double step = Math.PI / 2;
				Location loc = target.getLocation();

				public void run() {
					if ((step -= Math.PI / 30) <= 0)
						cancel();

					for (double i = 0; i < Math.PI * 2; i += Math.PI / 16)
						loc.getWorld().spawnParticle(Particle.WATER_DROP, loc.clone().add(Math.cos(i) * Math.sin(step) * 2, Math.cos(step) * 2, 2 * Math.sin(i) * Math.sin(step)), 0);
				}
			}.runTaskTimer(MMOItems.plugin, 0, 1);
		}
	});

	private ItemStack item;
	private String name;
	private ChatColor color;
	private ElementParticle particle;
	private ElementHandler handler;

	private Element(Material material, String name, ChatColor color, ElementParticle particle, ElementHandler handler) {
		this.item = new ItemStack(material);
		this.name = name;
		this.color = color;
		this.particle = particle;
		this.handler = handler;
	}

	public ItemStack getItem() {
		return item;
	}

	public String getName() {
		return name;
	}

	public ChatColor getPrefix() {
		return color;
	}

	public ElementParticle getParticle() {
		return particle;
	}

	public ElementHandler getHandler() {
		return handler;
	}

	public enum StatType {
		DAMAGE,
		DEFENSE;
	}

	public interface ElementHandler {
		public void elementAttack(TemporaryStats stats, AttackResult result, LivingEntity target, double damage, double absolute);
	}

	public static class ElementParticle {
		public final Consumer<Entity> display;

		public ElementParticle(Particle particle, double speed, int amount) {
			display = (entity) -> entity.getWorld().spawnParticle(particle, entity.getLocation().add(0, entity.getHeight() / 2, 0), amount, 0, 0, 0, speed);
		}

		public ElementParticle(Particle particle, float speed, int amount, Material material) {
			display = (entity) -> MMOItems.plugin.getVersion().getVersionWrapper().spawnParticle(particle, entity.getLocation().add(0, entity.getHeight() / 2, 0), amount, 0, 0, 0, speed, material);
		}

		public void displayParticle(Entity entity) {
			display.accept(entity);
		}
	}
}