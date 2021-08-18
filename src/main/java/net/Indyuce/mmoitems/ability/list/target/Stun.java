package net.Indyuce.mmoitems.ability.list.target;

import net.Indyuce.mmoitems.ability.TargetAbility;
import net.Indyuce.mmoitems.ability.metadata.TargetAbilityMetadata;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Stun extends TargetAbility {
    public Stun() {
        super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

        addModifier("cooldown", 10);
        addModifier("duration", 2);
        addModifier("mana", 0);
        addModifier("stamina", 0);
    }

    @Override
    public void whenCast(ItemAttackMetadata attack, TargetAbilityMetadata ability) {
        LivingEntity target = ability.getTarget();

        target.getWorld().playSound(target.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 2);
        target.getWorld().playEffect(target.getLocation(), Effect.STEP_SOUND, 42);
        target.getWorld().playEffect(target.getLocation().add(0, 1, 0), Effect.STEP_SOUND, 42);
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (ability.getModifier("duration") * 20), 254));
    }
}
