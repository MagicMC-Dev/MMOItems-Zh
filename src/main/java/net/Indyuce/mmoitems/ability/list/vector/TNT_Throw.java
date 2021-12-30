package net.Indyuce.mmoitems.ability.list.vector;

import io.lumine.mythic.lib.api.util.TemporaryListener;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.ability.VectorAbility;
import net.Indyuce.mmoitems.ability.metadata.VectorAbilityMetadata;
import io.lumine.mythic.lib.damage.AttackMetadata;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

public class TNT_Throw extends VectorAbility {
    public TNT_Throw() {
        super();

        addModifier("cooldown", 10);
        addModifier("force", 1);
        addModifier("mana", 0);
        addModifier("stamina", 0);
    }

    @Override
    public void whenCast(AttackMetadata attack, VectorAbilityMetadata ability) {
        Vector vec = ability.getTarget().multiply(2 * ability.getModifier("force"));
        TNTPrimed tnt = (TNTPrimed) attack.getPlayer().getWorld().spawnEntity(attack.getPlayer().getLocation().add(0, 1, 0), EntityType.PRIMED_TNT);
        tnt.setFuseTicks(80);
        tnt.setVelocity(vec);
        new CancelTeamDamage(attack.getPlayer(), tnt);
        attack.getPlayer().getWorld().playSound(attack.getPlayer().getLocation(), Sound.ENTITY_SNOWBALL_THROW, 1, 0);
        attack.getPlayer().getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, attack.getPlayer().getLocation().add(0, 1, 0), 12, 0, 0, 0, .1);
    }

	/**
	 * Used to cancel team damage and other things
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
			if (event.getDamager().equals(tnt) && !MMOUtils.canTarget(player, event.getEntity()))
				event.setCancelled(true);
		}

		@Override
		public void whenClosed() {
			// Nothing
		}
	}
}
