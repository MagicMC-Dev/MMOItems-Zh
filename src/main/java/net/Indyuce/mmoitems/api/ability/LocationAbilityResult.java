package net.Indyuce.mmoitems.api.ability;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import net.Indyuce.mmoitems.stat.data.AbilityData;

public class LocationAbilityResult extends AbilityResult {
	private final Location target;

	public LocationAbilityResult(AbilityData ability, Player caster, LivingEntity target) {
		super(ability);

		this.target = getTargetLocation(caster, target, 50);
	}

	public Location getTarget() {
		return target;
	}

	@Override
	public boolean isSuccessful() {
		return target != null;
	}

	private Location getTargetLocation(Player player, LivingEntity entity, int length) {
		if (entity != null)
			return entity.getLocation();

		Location loc = player.getTargetBlock((Set<Material>) null, length).getLocation();
		return loc.getBlock().getType() == Material.AIR ? null : loc.add(.5, 1, .5);
	}
}
