package net.Indyuce.mmoitems.api.interaction.weapon.untargeted;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.player.PlayerMetadata;

/**
 * These weapon types need to be adapted to raw YAML scripts.
 */
@Deprecated
public interface LegacyWeapon {

    @Deprecated
    boolean canAttack(boolean rightClick, EquipmentSlot slot);

    @Deprecated
    void applyAttackEffect(PlayerMetadata stats, EquipmentSlot slot);
}
