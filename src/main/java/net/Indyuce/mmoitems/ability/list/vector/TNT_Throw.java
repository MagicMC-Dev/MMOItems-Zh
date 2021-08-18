package net.Indyuce.mmoitems.ability.list.vector;

import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.ability.VectorAbility;
import net.Indyuce.mmoitems.ability.metadata.VectorAbilityMetadata;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import net.Indyuce.mmoitems.api.util.TemporaryListener;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

public class TNT_Throw extends VectorAbility implements Listener {
    public TNT_Throw() {
        super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK,
                CastingMode.SHIFT_RIGHT_CLICK);

        addModifier("cooldown", 10);
        addModifier("force", 1);
        addModifier("mana", 0);
        addModifier("stamina", 0);
    }

    @Override
    public void whenCast(ItemAttackMetadata attack, VectorAbilityMetadata ability) {
        Vector vec = ability.getTarget().multiply(2 * ability.getModifier("force"));
        TNTPrimed tnt = (TNTPrimed) attack.getDamager().getWorld().spawnEntity(attack.getDamager().getLocation().add(0, 1, 0), EntityType.PRIMED_TNT);
        tnt.setFuseTicks(80);
        tnt.setVelocity(vec);
        new CancelTeamDamage(attack.getDamager(), tnt);
        attack.getDamager().getWorld().playSound(attack.getDamager().getLocation(), Sound.ENTITY_SNOWBALL_THROW, 1, 0);
        attack.getDamager().getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, attack.getDamager().getLocation().add(0, 1, 0), 12, 0, 0, 0, .1);
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
