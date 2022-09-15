package net.Indyuce.mmoitems.api.interaction.weapon;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.comp.flags.CustomFlag;
import io.lumine.mythic.lib.damage.AttackMetadata;
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
        if (playerData.isEncumbered()) {
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
    public boolean checkAndApplyWeaponCosts() {
        if (checkWeaponCosts(null)) {
            applyWeaponCosts(0, null);
            return true;
        }

        return false;
    }

    /**
     * Checks for cooldown, mana and stamina weapon costs
     *
     * @return If requirements were met ie the attack can be cast successfully
     */
    public boolean checkWeaponCosts(@Nullable CooldownType cooldown) {
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

        return true;
    }

    /**
     * Applies cooldown, mana and stamina weapon costs
     *
     * @param attackDelay The weapon attack period/delay
     * @param cooldown    The weapon cooldown type. When set to null, no
     *                    cooldown will be applied. This is made to handle
     *                    custom weapons
     */
    public void applyWeaponCosts(double attackDelay, @Nullable CooldownType cooldown) {

        double manaCost = getNBTItem().getStat("MANA_COST");
        if (manaCost > 0)
            playerData.getRPG().giveMana(-manaCost);

        double staminaCost = getNBTItem().getStat("STAMINA_COST");
        if (staminaCost > 0)
            playerData.getRPG().giveStamina(-staminaCost);

        if (cooldown != null)
            getPlayerData().applyCooldown(cooldown, attackDelay);
    }

    /**
     * Only applies mana and stamina costs. Cooldown is not required for
     * targeted attacks since the vanilla attack bar already does that.
     *
     * @param attackMeta The attack being performed
     * @param target     The attack target
     * @return If the attack is successful, or if it was canceled otherwise
     */
    public boolean handleTargetedAttack(AttackMetadata attackMeta, LivingEntity target) {

        // Handle weapon mana and stamina costs ONLY
        if (!checkAndApplyWeaponCosts())
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

    /**
     * @return First argument, or second if zero or lower
     */
    public double requireNonZero(double number, double elseNumber) {
        return number <= 0 ? elseNumber : number;
    }
}
