package net.Indyuce.mmoitems.api.ability;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.stat.data.AbilityData;

public class VectorAbilityResult extends AbilityResult {
	private final Vector target;

	public VectorAbilityResult(AbilityData ability, Player caster, LivingEntity target) {
		super(ability);

		this.target = getTargetDirection(caster, target);
	}

	public Vector getTarget() {
		return target;
	}

	@Override
	public boolean isSuccessful() {
		return true;
	}

	protected Vector getTargetDirection(Player player, LivingEntity target) {
		return target == null ? player.getEyeLocation().getDirection() : target.getLocation().add(0, target.getHeight() / 2, 0).subtract(player.getLocation().add(0, 1.3, 0)).toVector().normalize();
	}
}
