package net.Indyuce.mmoitems.ability.list.simple;

import io.lumine.mythic.lib.api.util.TemporaryListener;
import io.lumine.mythic.lib.version.VersionSound;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.ability.SimpleAbility;
import net.Indyuce.mmoitems.ability.metadata.SimpleAbilityMetadata;
import io.lumine.mythic.lib.damage.AttackMetadata;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.scheduler.BukkitRunnable;

public class Bunny_Mode extends SimpleAbility {
    public Bunny_Mode() {
        super();

        addModifier("duration", 20);
        addModifier("jump-force", 1);
        addModifier("cooldown", 50);
        addModifier("speed", 1);
        addModifier("mana", 0);
        addModifier("stamina", 0);
	}

    @Override
    public void whenCast(AttackMetadata attack, SimpleAbilityMetadata ability) {
        double duration = ability.getModifier("duration") * 20;
        double y = ability.getModifier("jump-force");
        double xz = ability.getModifier("speed");

        new BukkitRunnable() {
            final BunnyHandler handler = new BunnyHandler(attack.getPlayer(), duration);
            int j = 0;

            public void run() {
                if (j++ > duration) {
                    handler.close(3 * 20);
                    cancel();
                    return;
                }

                if (attack.getPlayer().getLocation().add(0, -.5, 0).getBlock().getType().isSolid()) {
                    attack.getPlayer()
                            .setVelocity(attack.getPlayer().getEyeLocation().getDirection().setY(0).normalize().multiply(.8 * xz).setY(0.5 * y / xz));
                    attack.getPlayer().getWorld().playSound(attack.getPlayer().getLocation(), VersionSound.ENTITY_ENDER_DRAGON_FLAP.toSound(), 2, 1);
                    for (double a = 0; a < Math.PI * 2; a += Math.PI / 12)
                        attack.getPlayer().getWorld().spawnParticle(Particle.CLOUD, attack.getPlayer().getLocation(), 0, Math.cos(a), 0, Math.sin(a),
                                .2);
                }
            }
		}.runTaskTimer(MMOItems.plugin, 0, 1);

	}

	public static class BunnyHandler extends TemporaryListener {
		private final Player player;

		public BunnyHandler(Player player, double duration) {
			super(EntityDamageEvent.getHandlerList());

			this.player = player;

			close((long) (duration *20));
		}

		@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
		public void a(EntityDamageEvent event) {
			if (event.getEntity().equals(player) && event.getCause() == DamageCause.FALL)
				event.setCancelled(true);
		}

        @Override
        public void whenClosed() {
            // Nothing
        }
    }
}
