package net.Indyuce.mmoitems.ability.list.misc;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.ability.Ability;
import net.Indyuce.mmoitems.ability.metadata.SimpleAbilityMetadata;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Hoearthquake extends Ability<SimpleAbilityMetadata> {
    public Hoearthquake() {
        super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

        addModifier("cooldown", 10);
        addModifier("mana", 0);
        addModifier("stamina", 0);
    }

    @Override
    public SimpleAbilityMetadata canBeCast(ItemAttackMetadata attack, LivingEntity target, AbilityData ability) {
        return attack.getDamager().isOnGround() ? new SimpleAbilityMetadata(ability) : null;
    }

    @Override
    public void whenCast(ItemAttackMetadata attack, SimpleAbilityMetadata ability) {
        new BukkitRunnable() {
            final Vector vec = attack.getDamager().getEyeLocation().getDirection().setY(0);
            final Location loc = attack.getDamager().getLocation();
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
						if (b.getType() == Material.GRASS || b.getType() == Material.DIRT) {
                            BlockBreakEvent event = new BlockBreakEvent(b, attack.getDamager());
                            event.setDropItems(false);
                            Bukkit.getPluginManager().callEvent(event);
                            if (!event.isCancelled()) b.setType(Material.FARMLAND);
                        }
					}
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}
