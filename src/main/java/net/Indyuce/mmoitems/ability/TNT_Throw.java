package net.Indyuce.mmoitems.ability;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.ability.Ability;
import net.Indyuce.mmoitems.api.ability.AbilityResult;
import net.Indyuce.mmoitems.api.ability.VectorAbilityResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.api.util.TemporaryListener;
import net.Indyuce.mmoitems.stat.data.AbilityData;

public class TNT_Throw extends Ability implements Listener {
	public TNT_Throw() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK,
				CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("cooldown", 10);
		addModifier("force", 1);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public AbilityResult whenRan(CachedStats stats, LivingEntity target, AbilityData ability, ItemAttackResult result) {
		return new VectorAbilityResult(ability, stats.getPlayer(), target);
	}

	@Override
	public void whenCast(CachedStats stats, AbilityResult ability, ItemAttackResult result) {
		Vector vec = ((VectorAbilityResult) ability).getTarget().multiply(2 * ability.getModifier("force"));
		TNTPrimed tnt = (TNTPrimed) stats.getPlayer().getWorld().spawnEntity(stats.getPlayer().getLocation().add(0, 1, 0), EntityType.PRIMED_TNT);
		tnt.setFuseTicks(80);
		tnt.setVelocity(vec);
		new CancelTeamDamage(stats.getPlayer(), tnt);
		stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), Sound.ENTITY_SNOWBALL_THROW, 1, 0);
		stats.getPlayer().getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, stats.getPlayer().getLocation().add(0, 1, 0), 12, 0, 0, 0, .1);
	}

	/*
	 * used to cancel team damage and other things
	 */
	public static class CancelTeamDamage extends TemporaryListener {
		private final Player player;
		private final TNTPrimed tnt;

		public CancelTeamDamage(Player player, TNTPrimed tnt) {
			super(EntityDamageByEntityEvent.getHandlerList());

			this.player = player;
			this.tnt = tnt;

			close(100);
		}

		@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
		public void a(EntityDamageByEntityEvent event) {
			if (event.getDamager().equals(tnt) && !MMOUtils.canDamage(player, event.getEntity()))
				event.setCancelled(true);
		}
	}
}
