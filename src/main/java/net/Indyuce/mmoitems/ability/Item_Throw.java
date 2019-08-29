package net.Indyuce.mmoitems.ability;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.AttackResult;
import net.Indyuce.mmoitems.api.DamageInfo.DamageType;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.Indyuce.mmoitems.api.util.NoInteractItemEntity;
import net.Indyuce.mmoitems.stat.data.AbilityData;

public class Item_Throw extends Ability implements Listener {
	public Item_Throw() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("damage", 6);
		addModifier("force", 1);
		addModifier("cooldown", 10);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public void whenCast(TemporaryStats stats, LivingEntity target, AbilityData data, AttackResult result) {
		ItemStack itemStack = stats.getPlayer().getInventory().getItemInMainHand().clone();
		if (itemStack == null || itemStack.getType() == Material.AIR) {
			result.setSuccessful(false);
			return;
		}

		final NoInteractItemEntity item = new NoInteractItemEntity(stats.getPlayer().getLocation().add(0, 1.2, 0), itemStack);
		item.getEntity().setVelocity(getTargetDirection(stats.getPlayer(), target).multiply(1.5 * data.getModifier("force")));
		stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), Sound.ENTITY_SNOWBALL_THROW, 1, 0);
		new BukkitRunnable() {
			double ti = 0;

			public void run() {
				ti++;
				if (ti > 20 || item.getEntity().isDead()) {
					item.close();
					cancel();
				}

				item.getEntity().getWorld().spawnParticle(Particle.CRIT, item.getEntity().getLocation(), 0);
				for (Entity target : item.getEntity().getNearbyEntities(1, 1, 1))
					if (MMOUtils.canDamage(stats.getPlayer(), target)) {
						MMOItems.plugin.getDamage().damage(stats, (LivingEntity) target, data.getModifier("damage"), DamageType.SKILL, DamageType.PROJECTILE, DamageType.PHYSICAL);
						item.close();
						cancel();
					}
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}
