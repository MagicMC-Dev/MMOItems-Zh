package net.Indyuce.mmoitems.api;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.player.PlayerMetadata;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.PlayerData.CooldownType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @deprecated Move this over to MythicLib ffs
 *         Also implementation is bad af
 */
@Deprecated
public class ElementalAttack {
    private final PlayerMetadata attacker;
    private final PlayerData playerData;

    /**
     * Percentage of the initial damage being dealt as elemental damage
     */
    private final Map<Element, Double> relative = new HashMap<>();

    /**
     * Flat damage being dealt as elemental damage
     */
    private final Map<Element, Double> absolute = new HashMap<>();

    /**
     * Attack target saved because MI needs to
     * access the defense stats from that entity
     */
    private final LivingEntity target;

    /**
     * Damage that is not elemental
     */
    private final double regularDamage;

    /**
     * Initial attack damage (both regular and elemental)
     */
    private final double initialDamage;

    private static final Random RANDOM = new Random();

    public ElementalAttack(PlayerMetadata attacker, NBTItem item, double initialDamage, LivingEntity target) {
        this.initialDamage = initialDamage;
        this.playerData = PlayerData.get(attacker.getPlayer());
        this.attacker = attacker;
        this.target = target;

        double regularDamage = initialDamage;
        for (Element element : Element.values()) {
            double relativeDamage = item.getStat(element.name() + "_DAMAGE");
            if (relativeDamage > 0) {
                double flatElemental = relativeDamage / 100 * initialDamage;
                relative.put(element, relativeDamage);
                absolute.put(element, flatElemental);
                regularDamage -= flatElemental;
            }
        }

        this.regularDamage = regularDamage;
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
        double correctionCoeff = 1;
        if (!playerData.isOnCooldown(CooldownType.ELEMENTAL_ATTACK))
            for (Element element : relative.keySet()) {
                double relativeDamage = relative.get(element);
                double independentProbability = relativeDamage / 100;
                if (RANDOM.nextDouble() < independentProbability / correctionCoeff) {

                    // Perform elemental critical strike
                    playerData.applyCooldown(CooldownType.ELEMENTAL_ATTACK, 2);
                    element.getHandler().elementAttack(attacker, target, relativeDamage, absolute.get(element));

                    // Multiply corresponding damage by 2
                    absolute.put(element, absolute.get(element) * 2);
                    break;
                }
                correctionCoeff -= independentProbability;
            }

        // Calculate final damage again
        double finalDamage = regularDamage;

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
