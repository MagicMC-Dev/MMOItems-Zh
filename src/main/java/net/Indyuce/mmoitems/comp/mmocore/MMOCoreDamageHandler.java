package net.Indyuce.mmoitems.comp.mmocore;

import java.util.stream.Collectors;

import org.bukkit.entity.Entity;

import net.Indyuce.mmocore.comp.rpg.damage.DamageHandler;
import net.Indyuce.mmocore.comp.rpg.damage.DamageInfo;
import net.Indyuce.mmocore.comp.rpg.damage.DamageInfo.DamageType;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.AttackResult;

public class MMOCoreDamageHandler implements DamageHandler {

	@Override
	public DamageInfo getDamage(Entity entity) {
		AttackResult result = MMOItems.plugin.getDamage().getDamage(entity);
		return new DamageInfo(result.getDamage(), result.getTypes().stream().map((type) -> DamageType.valueOf(type.name())).collect(Collectors.toSet()));
	}

	@Override
	public boolean hasDamage(Entity entity) {
		return MMOItems.plugin.getDamage().isDamaged(entity);
	}
}
