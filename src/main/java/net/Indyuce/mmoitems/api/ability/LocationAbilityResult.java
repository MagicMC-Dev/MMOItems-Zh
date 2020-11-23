package net.Indyuce.mmoitems.api.ability;

import net.Indyuce.mmoitems.stat.data.AbilityData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class LocationAbilityResult extends AbilityResult {
	private final Location target;

	public LocationAbilityResult(AbilityData ability, Player caster, LivingEntity target) {
		super(ability);

		this.target = getTargetLocation(caster, target);
	}

	public Location getTarget() {
		return target;
	}

	@Override
	public boolean isSuccessful() {
		return target != null;
	}

	private Location getTargetLocation(Player player, LivingEntity entity) {
		if (entity != null)
			return entity.getLocation();

		Location loc = player.getTargetBlock(null, 50).getLocation();
		return loc.getBlock().getType() == Material.AIR ? null : loc.add(.5, 1, .5);
	}
}
