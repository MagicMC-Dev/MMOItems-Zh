package net.Indyuce.mmoitems.api;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.api.player.PlayerData.CooldownType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ElementalAttack {
	private final Map<Element, Double> relative = new HashMap<>();
	private final Map<Element, Double> absolute = new HashMap<>();
	private final ItemAttackMetadata attack;
	private final LivingEntity target;

	private static final Random random = new Random();

	// TODO rework this shit
	public ElementalAttack(NBTItem item, ItemAttackMetadata attack, LivingEntity target) {
		this.attack = attack;
		this.target = target;

		for (Element element : Element.values()) {
			double damage = item.getStat(element.name() + "_DAMAGE");
			if (damage > 0) {
				relative.put(element, damage);

				double abs = damage / 100 * attack.getDamage().getDamage();
				attack.getDamage().add(-abs);
				absolute.put(element, abs);
			}
		}
	}

	public void apply() {

		// elemental defense
		for (ItemStack equip : target.getEquipment().getArmorContents()) {
			NBTItem nbtEquip = MythicLib.plugin.getVersion().getWrapper().getNBTItem(equip);
			if (nbtEquip.getType() != null)
				for (Element element : absolute.keySet()) {
					double defense = nbtEquip.getStat(element.name() + "_DEFENSE") / 100;
					if (defense > 0) {
						relative.put(element, relative.get(element) * (1 - defense));
						absolute.put(element, absolute.get(element) * (1 - defense));
					}
				}
		}

		// elemental attacks
		double p = 1;
		if (!attack.getPlayerData().isOnCooldown(CooldownType.ELEMENTAL_ATTACK))
			for (Element element : relative.keySet()) {
				double damage = relative.get(element);
				if (random.nextDouble() < (damage / 100 / p)) {
					attack.getPlayerData().applyCooldown(CooldownType.ELEMENTAL_ATTACK, 2);
					element.getHandler().elementAttack(attack, target, damage, absolute.get(element));
					break;
				}
				p -= damage / 100;
			}

		for (Element element : absolute.keySet()) {
			double damage = absolute.get(element);
			if (damage > 0) {
				attack.getDamage().add(damage);
				element.getParticle().displayParticle(target);
			}
		}
	}
}
