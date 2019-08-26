package net.Indyuce.mmoitems.ability;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.AttackResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;

public class Hoearthquake extends Ability {
	public Hoearthquake() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("cooldown", 10);
		addModifier("mana", 0);
		addModifier("stamina", 0);

		if (!MMOItems.plugin.getVersion().isBelowOrEqual(1, 12))
			disable();
	}

	@Override
	public void whenCast(TemporaryStats stats, LivingEntity target, AbilityData data, AttackResult result) {
		if (!stats.getPlayer().isOnGround()) {
			result.setSuccessful(false);
			return;
		}

		new BukkitRunnable() {
			Vector vec = stats.getPlayer().getEyeLocation().getDirection().setY(0);
			Location loc = stats.getPlayer().getLocation();
			int ti = 0;

			public void run() {
				if (ti++ > 20)
					cancel();

				loc.add(vec);
				loc.getWorld().playSound(loc, Sound.BLOCK_GRAVEL_BREAK, 2, 1);
				loc.getWorld().spawnParticle(Particle.CLOUD, loc, 1, .5, 0, .5, 0);

				for (int x = -1; x < 2; x++)
					for (int z = -1; z < 2; z++) {
						Block b = loc.clone().add(x, -1, z).getBlock();
						if (b.getType() == Material.GRASS || b.getType() == Material.DIRT)
							b.setType(Material.FARMLAND);
					}
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}
