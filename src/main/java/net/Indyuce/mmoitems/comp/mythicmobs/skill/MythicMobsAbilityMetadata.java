package net.Indyuce.mmoitems.comp.mythicmobs.skill;

import net.Indyuce.mmoitems.ability.AbilityMetadata;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import org.bukkit.entity.LivingEntity;

public class MythicMobsAbilityMetadata extends AbilityMetadata {
    private final LivingEntity target;

    public MythicMobsAbilityMetadata(AbilityData ability, LivingEntity target) {
        super(ability);

        this.target = target;
    }

    public LivingEntity getTarget() {
        return target;
    }

    @Override
    public boolean isSuccessful() {
        return true;
    }
}