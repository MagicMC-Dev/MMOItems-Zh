package net.Indyuce.mmoitems.api.interaction.weapon.untargeted;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.player.PlayerMetadata;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.event.item.UntargetedWeaponUseEvent;
import net.Indyuce.mmoitems.api.interaction.util.UntargetedDurabilityItem;
import net.Indyuce.mmoitems.api.interaction.weapon.Weapon;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public abstract class UntargetedWeapon extends Weapon {
    protected final UntargetedWeaponType weaponType;

    public UntargetedWeapon(Player player, NBTItem item, UntargetedWeaponType weaponType) {
        super(player, item);

        this.weaponType = weaponType;
    }

    /**
     * Called when the player interacts with the item. This method is used to
     * apply durability and cast the weapon attack
     *
     * @param slot Slot being interacted with
     * @implNote Since MI 6.7.3 this method handles custom durability, cooldown
     *         checks and player stat snapshots.
     */
    public void handleTargetFreeAttack(EquipmentSlot slot) {
        if (!canAttack(slot))
            return;

        // Check for durability
        UntargetedDurabilityItem durItem = new UntargetedDurabilityItem(getPlayer(), getNBTItem(), slot);
        if (durItem.isBroken())
            return;

        // Apply weapon mana/stamina costs and cooldown
        PlayerMetadata stats = getPlayerData().getStats().newTemporary(slot);
        if (!checkWeaponCosts(PlayerData.CooldownType.BASIC_ATTACK))
            return;

        // Check for Bukkit event
        UntargetedWeaponUseEvent called = new UntargetedWeaponUseEvent(playerData, this);
        Bukkit.getPluginManager().callEvent(called);
        if (called.isCancelled())
            return;

        // Apply weapon costs
        double attackDelay = 1 / requireNonZero(stats.getStat("ATTACK_SPEED"), MMOItems.plugin.getConfig().getDouble("default.attack-speed"));
        applyWeaponCosts(attackDelay, PlayerData.CooldownType.BASIC_ATTACK);

        // Specific weapon attack effect
        applyAttackEffect(stats, slot);

        // Apply durability loss
        if (durItem.isValid())
            durItem.decreaseDurability(1).inventoryUpdate();
    }

    /**
     * Used for instance by the Crossbow item type to apply
     * ammo requirements. This is the first ever condition checked
     * when trying to perform an untargeted attack
     *
     * @param slot Slot used to attack
     * @return Extra attack condition, specific to the untargeted weapon type
     */
    public abstract boolean canAttack(EquipmentSlot slot);

    public abstract void applyAttackEffect(PlayerMetadata stats, EquipmentSlot slot);

    public UntargetedWeaponType getWeaponType() {
        return weaponType;
    }
}
