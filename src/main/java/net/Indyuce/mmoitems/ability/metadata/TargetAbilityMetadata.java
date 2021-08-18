package net.Indyuce.mmoitems.ability.metadata;

import io.lumine.mythic.lib.MythicLib;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.ability.AbilityMetadata;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class TargetAbilityMetadata extends AbilityMetadata {
    private final LivingEntity target;

    public TargetAbilityMetadata(AbilityData ability, Player caster, LivingEntity target) {
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
