package net.Indyuce.mmoitems.api.ability;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import io.lumine.mythic.lib.MythicLib;

public class TargetAbilityResult extends AbilityResult {
	private final LivingEntity target;

	public TargetAbilityResult(AbilityData ability, Player caster, LivingEntity target) {
		super(ability);

		this.target = target != null ? target : MythicLib.plugin.getVersion().getWrapper().rayTrace(caster, 50, entity -> MMOUtils.canDamage(caster, entity)).getHit();
	}
	
	public LivingEntity getTarget() {
		return target;
	}

	@Override
	public boolean isSuccessful() {
		return target != null;
	}
}
