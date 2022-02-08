package net.Indyuce.mmoitems.api;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.comp.target.InteractionType;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.version.VersionMaterial;
import io.lumine.mythic.lib.version.VersionSound;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.listener.ElementListener;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Consumer;
import org.bukkit.util.Vector;

public enum Element {
	FIRE(Material.BLAZE_POWDER, ChatColor.DARK_RED, new ElementParticle(Particle.FLAME, .05f, 8), (attacker, target, relative, absolute) -> {
		target.getWorld().spawnParticle(Particle.LAVA, target.getLocation().add(0, target.getHeight() / 2, 0), 14);
		target.getWorld().playSound(target.getLocation(), Sound.ENTITY_BLAZE_HURT, 2, .8f);
		target.setFireTicks((int) (relative * 2));
	}, 19, 25),

	ICE(VersionMaterial.SNOWBALL.toMaterial(), ChatColor.AQUA, new ElementParticle(Particle.BLOCK_CRACK, .07f, 16, Material.ICE),
			(attacker, target, relative, absolute) -> {
				new BukkitRunnable() {
					double y = 0;
					final Location loc = target.getLocation();

					public void run() {
						for (int j = 0; j < 3; j++) {
							if ((y += .07) >= 3)
								cancel();
							for (double k = 0; k < Math.PI * 2; k += Math.PI * 2 / 3)
								loc.getWorld().spawnParticle(Particle.REDSTONE, loc.clone().add(Math.cos(y * Math.PI + k) * (3 - y) / 2.5,
										y / 1.1, Math.sin(y * Math.PI + k) * (3 - y) / 2.5), 1, new Particle.DustOptions(Color.WHITE, 1));
						}
					}
				}.runTaskTimer(MMOItems.plugin, 0, 1);
				target.getWorld().playSound(target.getLocation(), Sound.BLOCK_GLASS_BREAK, 2, 0);
				target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (relative * 1.5), 5));
			}, 20, 24),

	WIND(Material.FEATHER, ChatColor.GRAY, new ElementParticle(Particle.EXPLOSION_NORMAL, .06f, 8), (attacker, target, relative, absolute) -> {
		target.getWorld().playSound(target.getLocation(), VersionSound.ENTITY_ENDER_DRAGON_GROWL.toSound(), 2, 2f);
		Vector vec = target.getLocation().subtract(attacker.getPlayer().getLocation()).toVector().normalize().multiply(1.7).setY(.5);
		target.setVelocity(vec);
		for (Entity entity : target.getNearbyEntities(3, 1, 3))
			if (MMOUtils.canTarget(attacker.getPlayer(), entity, InteractionType.OFFENSE_ACTION)) {
				entity.playEffect(EntityEffect.HURT);
				entity.setVelocity(vec);
			}
		for (double k = 0; k < Math.PI * 2; k += Math.PI / 16)
			target.getWorld().spawnParticle(Particle.CLOUD, target.getLocation().add(0, target.getHeight() / 2, 0), 0, Math.cos(k), .01,
					Math.sin(k), .15);
	}, 28, 34),

	EARTH(VersionMaterial.OAK_SAPLING.toMaterial(), ChatColor.GREEN, new ElementParticle(Particle.BLOCK_CRACK, .05f, 24, Material.DIRT),
			(attacker, target, relative, absolute) -> {
				target.getWorld().playSound(target.getLocation(), Sound.BLOCK_GRASS_BREAK, 2, 0);
				target.getWorld().spawnParticle(Particle.BLOCK_CRACK, target.getLocation().add(0, .1, 0), 64, 1, 0, 1,
						Material.DIRT.createBlockData());

				target.setVelocity(new Vector(0, 1, 0));
				for (Entity entity : target.getNearbyEntities(3, 1, 3))
					if (MMOUtils.canTarget(attacker.getPlayer(), entity, InteractionType.OFFENSE_ACTION))
						entity.setVelocity(new Vector(0, 1, 0));
			}, 29, 33),

	THUNDER(VersionMaterial.GUNPOWDER.toMaterial(), ChatColor.YELLOW, new ElementParticle(Particle.FIREWORKS_SPARK, .05f, 8), (attacker, target, relative, absolute) -> {
		target.getWorld().playSound(target.getLocation(), VersionSound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST.toSound(), 2, 0);
		for (Entity entity : target.getNearbyEntities(3, 2, 3))
			if (MMOUtils.canTarget(attacker.getPlayer(), entity, InteractionType.OFFENSE_ACTION))
				MythicLib.plugin.getDamage().damage(new ItemAttackMetadata(new DamageMetadata(absolute, DamageType.WEAPON), attacker), (LivingEntity) entity);

		for (double k = 0; k < Math.PI * 2; k += Math.PI / 16)
			target.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, target.getLocation().add(0, target.getHeight() / 2, 0), 0, Math.cos(k), .01,
					Math.sin(k), .18);
	}, 30, 32),

	WATER(VersionMaterial.LILY_PAD.toMaterial(), ChatColor.BLUE, new ElementParticle(Particle.BLOCK_CRACK, .07f, 32, Material.WATER),
			(attacker, target, damage, absolute) -> {
				ElementListener.weaken(target);
				new BukkitRunnable() {
					double step = Math.PI / 2;
					final Location loc = target.getLocation();

					public void run() {
						if ((step -= Math.PI / 30) <= 0)
							cancel();

						for (double i = 0; i < Math.PI * 2; i += Math.PI / 16)
							loc.getWorld().spawnParticle(Particle.WATER_DROP,
									loc.clone().add(Math.cos(i) * Math.sin(step) * 2, Math.cos(step) * 2, 2 * Math.sin(i) * Math.sin(step)), 0);
					}
				}.runTaskTimer(MMOItems.plugin, 0, 1);
			}, 37, 43),

	LIGHTNESS(Material.GLOWSTONE_DUST, ChatColor.WHITE, new ElementParticle(Particle.BLOCK_CRACK, .07f, 32, Material.WHITE_WOOL),
			(attacker, target, relative, absolute) -> {
				// TODO
			}, 38, 42),

	DARKNESS(Material.COAL, ChatColor.DARK_GRAY, new ElementParticle(Particle.BLOCK_CRACK, .07f, 32, Material.COAL_BLOCK), (attacker, target, relative, absolute) -> {
		// TODO
	}, 39, 41),

	;

	private final ItemStack item;
	private final String name;
	private final ChatColor color;
	private final ElementParticle particle;
	private final ElementHandler handler;
	private final int damageGuiSlot, defenseGuiSlot;

	Element(Material material, ChatColor color, ElementParticle particle, ElementHandler handler, int damageGuiSlot, int defenseGuiSlot) {
		this.item = new ItemStack(material);
		this.name = MMOUtils.caseOnWords(name().toLowerCase());
		this.color = color;
		this.particle = particle;

		this.handler = handler;
		this.damageGuiSlot = damageGuiSlot;
		this.defenseGuiSlot = defenseGuiSlot;
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

	public int getDamageGuiSlot() {
		return damageGuiSlot;
	}

	public int getDefenseGuiSlot() {
		return defenseGuiSlot;
	}

    @FunctionalInterface
    public interface ElementHandler {

        /**
         * @param attacker Player performing elemental attack
         * @param target   Attack target
         * @param damage   Relative elemental damage
         * @param absolute Absolute elemental damage dealt
         */
        void elementAttack(PlayerMetadata attacker, LivingEntity target, double damage, double absolute);
    }

	public static class ElementParticle {
		public final Consumer<Entity> display;

		public ElementParticle(Particle particle, double speed, int amount) {
			display = (entity) -> entity.getWorld().spawnParticle(particle, entity.getLocation().add(0, entity.getHeight() / 2, 0), amount, 0, 0, 0,
					speed);
		}

		public ElementParticle(Particle particle, float speed, int amount, Material material) {
			display = (entity) -> entity.getWorld().spawnParticle(particle, entity.getLocation().add(0, entity.getHeight() / 2, 0), amount, 0, 0, 0,
					speed, material.createBlockData());
		}

		public void displayParticle(Entity entity) {
			display.accept(entity);
		}
	}
}