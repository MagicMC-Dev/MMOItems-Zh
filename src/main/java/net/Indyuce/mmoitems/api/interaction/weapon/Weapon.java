package net.Indyuce.mmoitems.api.interaction.weapon;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.comp.flags.CustomFlag;
import io.lumine.mythic.lib.damage.AttackMetadata;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.api.interaction.UseItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.PlayerData.CooldownType;
import net.Indyuce.mmoitems.api.util.message.Message;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

public class Weapon extends UseItem {
    public Weapon(Player player, NBTItem item) {
        this(PlayerData.get(player), item);
    }

    public Weapon(PlayerData playerData, NBTItem item) {
        super(playerData, item);
    }

    @Override
    public boolean checkItemRequirements() {
        if (playerData.areHandsFull()) {
            Message.HANDS_TOO_CHARGED.format(ChatColor.RED).send(getPlayer());
            return false;
        }

        // Check for class, level... then flags
        return playerData.getRPG().canUse(getNBTItem(), true) && MythicLib.plugin.getFlags().isFlagAllowed(getPlayer(), CustomFlag.MI_WEAPONS);
    }

    /**
     * Only applies mana and stamina weapon costs
     *
     * @return If the attack was cast successfully
     */
    public boolean applyWeaponCosts() {
        return applyWeaponCosts(0, null);
    }

    /**
     * Applies cooldown, mana and stamina weapon costs
     *
     * @param attackSpeed The weapon attack speed
     * @param cooldown    The weapon cooldown type. When set to null, no
     *                    cooldown will be applied. This is made to handle
     *                    custom weapons
     * @return If requirements were met ie the attack was cast successfully
     */
    public boolean applyWeaponCosts(double attackSpeed, @Nullable CooldownType cooldown) {
        if (cooldown != null && getPlayerData().isOnCooldown(cooldown))
            return false;

        double manaCost = getNBTItem().getStat("MANA_COST");
        if (manaCost > 0 && playerData.getRPG().getMana() < manaCost) {
            Message.NOT_ENOUGH_MANA.format(ChatColor.RED).send(getPlayer());
            return false;
        }

        double staminaCost = getNBTItem().getStat("STAMINA_COST");
        if (staminaCost > 0 && playerData.getRPG().getStamina() < staminaCost) {
            Message.NOT_ENOUGH_STAMINA.format(ChatColor.RED).send(getPlayer());
            return false;
        }

        if (manaCost > 0)
            playerData.getRPG().giveMana(-manaCost);

        if (staminaCost > 0)
            playerData.getRPG().giveStamina(-staminaCost);

        if (cooldown != null)
            getPlayerData().applyCooldown(cooldown, attackSpeed);

        return true;
    }

    /**
     * @param attackMeta The attack being performed
     * @param target     The attack target
     * @return If the attack is successful, or if it was canceled otherwise
     */
    public boolean handleTargetedAttack(AttackMetadata attackMeta, LivingEntity target) {

        // Handle weapon cooldown, mana and stamina costs
        double attackSpeed = getNBTItem().getStat(ItemStats.ATTACK_SPEED.getId());
        attackSpeed = attackSpeed == 0 ? 1.493 : 1 / attackSpeed;
        if (!applyWeaponCosts())
            return false;

        // Handle item set attack effects
        if (getMMOItem().getType().getItemSet().hasAttackEffect() && !getNBTItem().getBoolean("MMOITEMS_DISABLE_ATTACK_PASSIVE"))
            getMMOItem().getType().getItemSet().applyAttackEffect(attackMeta, playerData, target, this);

        return true;
    }

    protected Location getGround(Location loc) {
        for (int j = 0; j < 20; j++) {
            if (loc.getBlock().getType().isSolid())
                return loc;
            loc.add(0, -1, 0);
        }
        return loc;
    }

    // returns default getValue if stat equals 0
    public double getValue(double a, double def) {
        return a <= 0 ? def : a;
    }
}
