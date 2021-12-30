package net.Indyuce.mmoitems.ability.metadata;

import net.Indyuce.mmoitems.stat.data.AbilityData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Item that requires to throw the item in hand.
 * This takes as input the player's main hand item and
 * also takes the direction where he's looking
 *
 * @deprecated Abilities were moved over to MythicLib.
 *         AbilityMetadata from MMOItems are now {@link io.lumine.mythic.lib.skill.result.SkillResult}
 */
@Deprecated
public class ItemAbilityMetadata extends VectorAbilityMetadata {
    private final ItemStack item;

    public ItemAbilityMetadata(AbilityData ability, Player caster, LivingEntity target) {
        super(ability, caster, target);

        // TODO getItemInMainHand? Breaks the ability when used in the offhand.
        item = caster.getInventory().getItemInMainHand();
    }

    public ItemStack getItem() {
        return item;
    }

    @Override
    public boolean isSuccessful() {
        return item != null;
    }
}