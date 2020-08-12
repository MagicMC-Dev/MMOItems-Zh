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
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.ability.Ability;
import net.Indyuce.mmoitems.api.ability.AbilityResult;
import net.Indyuce.mmoitems.api.ability.SimpleAbilityResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;

public class Hoearthquake extends Ability {
	public Hoearthquake() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("cooldown", 10);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public AbilityResult whenRan(CachedStats stats, LivingEntity target, AbilityData ability, ItemAttackResult result) {
		return new SimpleAbilityResult(ability, stats.getPlayer().isOnGround());
	}

	@Override
	public void whenCast(CachedStats stats, AbilityResult ability, ItemAttackResult result) {
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
