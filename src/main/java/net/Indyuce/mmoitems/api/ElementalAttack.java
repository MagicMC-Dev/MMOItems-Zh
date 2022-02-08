package net.Indyuce.mmoitems.api;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.player.PlayerMetadata;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.PlayerData.CooldownType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Debug;

import java.nio.file.WatchKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @deprecated Move this over to MythicLib ffs
 * Also implementation is bad af
 */
@Deprecated
public class ElementalAttack {
	private final PlayerMetadata attacker;
	private final PlayerData playerData;
	private final Map<Element, Double> relative = new HashMap<>();
	private final Map<Element, Double> absolute = new HashMap<>();
	private final LivingEntity target;
	private final double initialDamage;

	private static final Random random = new Random();

	public ElementalAttack(PlayerMetadata attacker, NBTItem item, double initialDamage, LivingEntity target) {
		this.initialDamage = initialDamage;
		this.playerData = PlayerData.get(attacker.getPlayer());
		this.attacker = attacker;
		this.target = target;

		for (Element element : Element.values()) {
			double damage = item.getStat(element.name() + "_DAMAGE");
			if (damage > 0) {
				relative.put(element, damage);
				absolute.put(element, damage / 100 * initialDamage);
			}
		}
	}

	public double getDamageModifier() {

		// Elemental defense
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

		// Elemental attacks
		double p = 1;
		if (!playerData.isOnCooldown(CooldownType.ELEMENTAL_ATTACK))
			for (Element element : relative.keySet()) {
				double relativeDamage = relative.get(element);
				if (random.nextDouble() < (relativeDamage / 100 / p)) {
					playerData.applyCooldown(CooldownType.ELEMENTAL_ATTACK, 2);
					element.getHandler().elementAttack(attacker, target, relativeDamage, absolute.get(element));
					absolute.put(element, absolute.get(element) * 2);
					break;
				}
				p -= relativeDamage / 100;
			}

		// Calculate final damage again
		double finalDamage = 0;

		for (Element element : absolute.keySet()) {
			double partialDamage = absolute.get(element);
			if (partialDamage > 0) {
				finalDamage += partialDamage;
				element.getParticle().displayParticle(target);
			}
		}

		return finalDamage - initialDamage;
	}
}
