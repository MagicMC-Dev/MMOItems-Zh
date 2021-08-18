package net.Indyuce.mmoitems.ability.list.simple;

import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import io.lumine.mythic.lib.version.VersionSound;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.ability.SimpleAbility;
import net.Indyuce.mmoitems.ability.metadata.SimpleAbilityMetadata;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class Shadow_Veil extends SimpleAbility implements Listener {
    public Shadow_Veil() {
        super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK,
                CastingMode.SHIFT_RIGHT_CLICK);

        addModifier("cooldown", 35);
        addModifier("duration", 5);
        addModifier("deception", 1);
        addModifier("mana", 0);
        addModifier("stamina", 0);
    }

    @Override
    public void whenCast(ItemAttackMetadata attack, SimpleAbilityMetadata ability) {
        double duration = ability.getModifier("duration");

        attack.getDamager().getWorld().playSound(attack.getDamager().getLocation(), VersionSound.ENTITY_ENDERMAN_TELEPORT.toSound(), 3, 0);
        for (Player online : Bukkit.getOnlinePlayers())
            online.hidePlayer(MMOItems.plugin, attack.getDamager());

        /*
         * clears the target of any entity around the player
         */
        for (Mob serverEntities : attack.getDamager().getWorld().getEntitiesByClass(Mob.class))
            if (serverEntities.getTarget() != null && serverEntities.getTarget().equals(attack.getDamager()))
                serverEntities.setTarget(null);

        ShadowVeilHandler svh = new ShadowVeilHandler(attack.getDamager(), duration);
        svh.setDeceptions(SilentNumbers.floor(ability.getModifier("deception")));
    }

	public static class ShadowVeilHandler extends BukkitRunnable implements Listener {
		private final Player player;
		private final double duration;
		private final Location loc;

		int deceptions = 1;
		public void setDeceptions(int dec) { deceptions = dec; }

		double ti = 0;
		double y = 0;
		boolean cancelled;

		public ShadowVeilHandler(Player player, double duration) {
			this.player = player;
			this.duration = duration;
			this.loc = player.getLocation();

			runTaskTimer(MMOItems.plugin, 0, 1);
			Bukkit.getPluginManager().registerEvents(this, MMOItems.plugin);
		}

		private void close() {
			if (ti < 0)
				return;

			player.getWorld().spawnParticle(Particle.SMOKE_LARGE, player.getLocation().add(0, 1, 0), 32, 0, 0, 0, .13);
			player.getWorld().playSound(player.getLocation(), VersionSound.ENTITY_ENDERMAN_TELEPORT.toSound(), 3, 0);

			// sets time to -1 so that next calls know the handler has already
			// been closed
			ti = -1;
			EntityDamageByEntityEvent.getHandlerList().unregister(this);
			EntityTargetEvent.getHandlerList().unregister(this);

			for (Player online : Bukkit.getOnlinePlayers())
				online.showPlayer(MMOItems.plugin, player);

			cancel();
		}

		@Override
		public void run() {
			if (ti++ > duration * 20 || !player.isOnline()) {
				close();
				return;
			}

			if (y < 4)
				for (int j1 = 0; j1 < 5; j1++) {
					y += .04;
					for (int j = 0; j < 4; j++) {
						double a = y * Math.PI * .8 + (j * Math.PI / 2);
						player.getWorld().spawnParticle(Particle.SMOKE_LARGE, loc.clone().add(Math.cos(a) * 2.5, y, Math.sin(a) * 2.5), 0);
					}
				}

		}

		@EventHandler
		public void cancelShadowVeil(EntityDamageByEntityEvent event) {
			if (event.getDamager().equals(player)) {
				deceptions--;
				if (deceptions <= 0) { close(); } }
		}

		@EventHandler
		public void cancelMobTarget(EntityTargetEvent event) {
			if (event.getTarget() != null && event.getTarget().equals(player))
				event.setCancelled(true);
		}
	}
}
