package net.Indyuce.mmoitems.ability.list.target;

import net.Indyuce.mmoitems.ability.TargetAbility;
import net.Indyuce.mmoitems.ability.metadata.TargetAbilityMetadata;
import io.lumine.mythic.lib.damage.AttackMetadata;
import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;

public class Bloodbath extends TargetAbility {
    public Bloodbath() {
        super();

        addModifier("amount", 2);
        addModifier("cooldown", 8);
        addModifier("mana", 0);
        addModifier("stamina", 0);
    }

    @Override
    public void whenCast(AttackMetadata attack, TargetAbilityMetadata ability) {
        LivingEntity target = ability.getTarget();

        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_COW_HURT, 1, 2);
        target.getWorld().playEffect(target.getLocation().add(0, 1, 0), Effect.STEP_SOUND, 152);
        attack.getPlayer().setFoodLevel((int) Math.min(20, attack.getPlayer().getFoodLevel() + ability.getModifier("amount")));
    }
}
