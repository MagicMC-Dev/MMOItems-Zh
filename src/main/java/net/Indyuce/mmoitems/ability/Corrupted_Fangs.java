package net.Indyuce.mmoitems.ability;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.ability.Ability;
import net.Indyuce.mmoitems.api.ability.AbilityResult;
import net.Indyuce.mmoitems.api.ability.VectorAbilityResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.api.util.TemporaryListener;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import io.lumine.mythic.lib.api.AttackResult;
import io.lumine.mythic.lib.api.DamageType;

public class Corrupted_Fangs extends Ability implements Listener {
	public Corrupted_Fangs() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK,
				CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("damage", 5);
		addModifier("cooldown", 12);
		addModifier("mana", 0);
		addModifier("stamina", 0);
		addModifier("fangs", 6);
	}

	@Override
	public AbilityResult whenRan(CachedStats stats, LivingEntity target, AbilityData ability, ItemAttackResult result) {
		return new VectorAbilityResult(ability, stats.getPlayer(), target);
	}

	@Override
	public void whenCast(CachedStats stats, AbilityResult ability, ItemAttackResult result) {

		stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), Sound.ENTITY_WITHER_SHOOT, 2, 2);
		new BukkitRunnable() {
			double fangAmount = ability.getModifier("fangs");
			final Vector vec = ((VectorAbilityResult) ability).getTarget().setY(0).multiply(2);
			final Location loc = stats.getPlayer().getLocation();
			double ti = 0;
			final FangsHandler handler = new FangsHandler(stats, ability.getModifier("damage"));

			public void run() {
				if (ti++ >= fangAmount) {
					handler.close(3 * 20);
					cancel();
					return;
				}

				loc.add(vec);
				EvokerFangs evokerFangs = (EvokerFangs) stats.getPlayer().getWorld().spawnEntity(loc, EntityType.EVOKER_FANGS);
				handler.entities.add(evokerFangs.getEntityId());
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}

	public class FangsHandler extends TemporaryListener {
		private final List<Integer> entities = new ArrayList<>();
		private final CachedStats stats;
		private final double damage;

		public FangsHandler(CachedStats stats, double damage) {
			super(EntityDamageByEntityEvent.getHandlerList());

			this.stats = stats;
			this.damage = damage;
		}

		@EventHandler
		public void a(EntityDamageByEntityEvent event) {
			if (event.getDamager() instanceof EvokerFangs && entities.contains(event.getDamager().getEntityId())
					&& MMOUtils.canDamage(stats.getPlayer(), event.getEntity()))
				new AttackResult(damage, DamageType.SKILL, DamageType.MAGIC).damage(stats.getPlayer(), (LivingEntity) event.getEntity());
		}
	}
}
