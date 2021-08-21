package net.Indyuce.mmoitems.ability.list.target;

import net.Indyuce.mmoitems.ability.TargetAbility;
import net.Indyuce.mmoitems.ability.metadata.TargetAbilityMetadata;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;

public class Bloodbath extends TargetAbility {
    public Bloodbath() {
        super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

        addModifier("amount", 2);
        addModifier("cooldown", 8);
        addModifier("mana", 0);
        addModifier("stamina", 0);
    }

    @Override
    public void whenCast(ItemAttackMetadata attack, TargetAbilityMetadata ability) {
        LivingEntity target = ability.getTarget();

        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_COW_HURT, 1, 2);
        target.getWorld().playEffect(target.getLocation().add(0, 1, 0), Effect.STEP_SOUND, 152);
        attack.getDamager().setFoodLevel((int) Math.min(20, attack.getDamager().getFoodLevel() + ability.getModifier("amount")));
    }
}
