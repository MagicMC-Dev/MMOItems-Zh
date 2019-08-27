package net.Indyuce.mmoitems.listener;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Message;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.PlayerData.CooldownType;
import net.Indyuce.mmoitems.api.player.PlayerStats;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.version.VersionSound;

public class MitigationListener implements Listener {
	private static final Random random = new Random();
	private static final List<DamageCause> mitigationCauses = Arrays.asList(DamageCause.PROJECTILE, DamageCause.ENTITY_ATTACK, DamageCause.ENTITY_EXPLOSION, DamageCause.ENTITY_SWEEP_ATTACK);

	@EventHandler(priority = EventPriority.HIGH)
	public void a(EntityDamageEvent event) {
		if (event.isCancelled() || !(event.getEntity() instanceof Player) || !mitigationCauses.contains(event.getCause()) || event.getEntity().hasMetadata("NPC"))
			return;

		Player player = (Player) event.getEntity();
		PlayerData playerData = PlayerData.get(player);
		PlayerStats stats = playerData.getStats();

		// dodging
		double dodgeRating = Math.min(stats.getStat(ItemStat.DODGE_RATING), MMOItems.plugin.getConfig().getDouble("mitigation.dodge.rating-max")) / 100;
		if (random.nextDouble() < dodgeRating && !playerData.isOnCooldown(CooldownType.DODGE)) {
			playerData.applyCooldown(CooldownType.DODGE, stats.getStat(ItemStat.DODGE_COOLDOWN_REDUCTION));
			event.setCancelled(true);

			Message.ATTACK_DODGED.format(ChatColor.RED).send(player, "mitigation");
			player.getWorld().playSound(player.getLocation(), VersionSound.ENTITY_ENDER_DRAGON_FLAP.toSound(), 2, 1);
			player.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, player.getLocation(), 16, 0, 0, 0, .06);
			if (MMOItems.plugin.getLanguage().dodgeKnockbackEnabled)
				player.setVelocity(getVector(player, event).multiply(.85 * MMOItems.plugin.getLanguage().dodgeKnockbackForce).setY(.3));
			return;
		}

		// parrying
		double parryRating = Math.min(stats.getStat(ItemStat.PARRY_RATING), MMOItems.plugin.getConfig().getDouble("mitigation.parry.rating-max")) / 100;
		if (random.nextDouble() < parryRating && !playerData.isOnCooldown(CooldownType.PARRY)) {
			playerData.applyCooldown(CooldownType.PARRY, stats.getStat(ItemStat.PARRY_COOLDOWN_REDUCTION));
			event.setCancelled(true);

			Message.ATTACK_PARRIED.format(ChatColor.RED).send(player, "mitigation");
			player.getWorld().playSound(player.getLocation(), VersionSound.ENTITY_ENDER_DRAGON_FLAP.toSound(), 2, 1);
			player.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, player.getLocation(), 16, 0, 0, 0, .06);
			if (event instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) event).getDamager() instanceof LivingEntity) {
				LivingEntity attacker = (LivingEntity) ((EntityDamageByEntityEvent) event).getDamager();
				attacker.setVelocity(attacker.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().setY(.35).multiply(MMOItems.plugin.getConfig().getDouble("mitigation.parry.knockback-force")));
			}
			return;
		}

		// blocking
		double blockRating = Math.min(stats.getStat(ItemStat.BLOCK_RATING), MMOItems.plugin.getConfig().getDouble("mitigation.block.rating-max")) / 100;
		if (random.nextDouble() < blockRating && !playerData.isOnCooldown(CooldownType.BLOCK)) {
			double blockPower = Math.min(MMOItems.plugin.getConfig().getDouble("mitigation.block.power.default") + stats.getStat(ItemStat.BLOCK_POWER), MMOItems.plugin.getConfig().getDouble("mitigation.block.power.max")) / 100;
			playerData.applyCooldown(CooldownType.BLOCK, stats.getStat(ItemStat.BLOCK_COOLDOWN_REDUCTION));
			event.setDamage(event.getDamage() * (1 - blockPower));

			Message.ATTACK_BLOCKED.format(ChatColor.RED, "#percent#", new DecimalFormat("0.#").format(blockPower * 100)).send(player, "mitigation");
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 2, 1);

			double yaw = getYaw(player, getVector(player, event)) + 95;
			for (double j = yaw - 90; j < yaw + 90; j += 5)
				for (double y = 0; y < 2; y += .1)
					MMOItems.plugin.getVersion().getVersionWrapper().spawnParticle(Particle.REDSTONE, player.getLocation().clone().add(Math.cos(Math.toRadians(j)) * .7, y, Math.sin(Math.toRadians(j)) * .7), Color.GRAY);
		}
	}

	private Vector getVector(Player player, EntityDamageEvent event) {
		return event instanceof EntityDamageByEntityEvent ? player.getLocation().subtract(((EntityDamageByEntityEvent) event).getDamager().getLocation()).toVector().normalize() : player.getEyeLocation().getDirection();
	}

	private double getYaw(Entity player, Vector vec) {
		return new Location(player.getWorld(), vec.getX(), vec.getY(), vec.getZ()).setDirection(vec).getYaw();
	}
}
