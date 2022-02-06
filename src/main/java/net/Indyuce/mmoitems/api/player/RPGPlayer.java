package net.Indyuce.mmoitems.api.player;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.player.cooldown.CooldownInfo;
import io.lumine.mythic.lib.skill.SkillMetadata;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.Indyuce.mmoitems.stat.type.ItemRestriction;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;

/**
 * Interface class between RPG core plugins like Heroes, MythicCore, SkillAPI
 * and MMOItems
 *
 * @author indyuce
 */
public abstract class RPGPlayer {
    private final PlayerData playerData;
    private final Player player;

    @Deprecated
    public RPGPlayer(Player player) {
        this(PlayerData.get(player));
    }


    /**
     * Used to retrieve useful information like class name, level, mana and
     * stamina from RPG plugins. This instance is reloaded everytime the player
     * logs back on the server to ensure the object references are kept up to
     * date
     *
     * @param playerData The corresponding player
     */
    public RPGPlayer(@NotNull PlayerData playerData) {
        this.player = playerData.getPlayer();
        this.playerData = playerData;
    }

    public PlayerData getPlayerData() {
        return playerData;
    }

    public Player getPlayer() {
        return playerData.getPlayer();
    }

    /**
     * Main profile level of the player.
     * <p></p>
     * Used in the REQUIRED_LEVEL item stat, and crafting stations with the level condition.
     */
    public abstract int getLevel();

    public abstract String getClassName();

    /**
     * The mana the player currently has.
     * <p></p>
     * Sometimes an internal quantity, sometimes the hunger bar, etc...
     */
    public abstract double getMana();

    public abstract double getStamina();

    /**
     * Sets the mana of the player.
     * <p></p>
     * Sometimes an internal quantity, sometimes the hunger bar, etc...
     */
    public abstract void setMana(double value);

    public abstract void setStamina(double value);

    /**
     * Increases or substracts mana to the player.
     * <p></p>
     * Sometimes an internal quantity, sometimes the hunger bar, etc...
     */
    public void giveMana(double value) {
        setMana(getMana() + value);
    }

    public void giveStamina(double value) {
        setStamina(getStamina() + value);
    }

    /**
     * If this item can be used by this player
     *
     * @param message Should the player be notified that they cant use the item?
     *                <p>Use for active checks (the player actually clicking)</p>
     */
    public boolean canUse(NBTItem item, boolean message) {
        return canUse(item, message, false);
    }

    /**
     * Calculates passive and dynamic item requirements. This does NOT apply item costs
     * like Mana or Stamina costs and does not check for required stamina or mana either.
     * Mana, stamina and cooldowns are handled in Weapon.applyWeaponCosts()
     *
     * @param message      Should the player be notified that they cant use the item?
     *                     <p>Use for active checks (the player actually clicking)</p>
     * @param allowDynamic If a Stat Restriction is dynamic, it will be ignored
     *                     if it fails (returning true even if it is not met).
     * @see ItemRestriction#isDynamic()
     */
    public boolean canUse(NBTItem item, boolean message, boolean allowDynamic) {
        if (item.hasTag("MMOITEMS_UNIDENTIFIED_ITEM")) {
            if (message) {
                Message.UNIDENTIFIED_ITEM.format(ChatColor.RED).send(player.getPlayer());
                player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1.5f);
            }
            return false;
        }

        //REQ//MMOItems. Log("Checking REQS");
        for (ItemRestriction condition : MMOItems.plugin.getStats().getItemRestrictionStats())
            if (!condition.isDynamic() || !allowDynamic)
                if (!condition.canUse(this, item, message))
                    return false;

        //REQ//MMOItems. Log(" \u00a7a> Success use");
        return true;
    }

    /**
     * This does not apply ability costs like Mana or Stamina costs. This does not
     * apply ability cooldown either but it does check if the ability is still on cooldown
     *
     * @param data Ability being cast
     * @return If the player can cast the ability
     * @deprecated Replaced by {@link AbilityData#getResult(SkillMetadata)}
     */
    @Deprecated
    public boolean canCast(AbilityData data) {

        if (playerData.getMMOPlayerData().getCooldownMap().isOnCooldown(data)) {
            CooldownInfo info = playerData.getMMOPlayerData().getCooldownMap().getInfo(data);
            if (!data.getTrigger().isSilent()) {
                StringBuilder progressBar = new StringBuilder(ChatColor.YELLOW + "");
                double progress = (double) (info.getInitialCooldown() - info.getRemaining()) / info.getInitialCooldown() * 10;
                String barChar = MMOItems.plugin.getConfig().getString("cooldown-progress-bar-char");
                for (int j = 0; j < 10; j++)
                    progressBar.append(progress >= j ? ChatColor.GREEN : ChatColor.WHITE).append(barChar);
                Message.SPELL_ON_COOLDOWN.format(ChatColor.RED, "#left#", "" + new DecimalFormat("0.#").format(info.getRemaining() / 1000d), "#progress#",
                        progressBar.toString(), "#s#", (info.getRemaining() > 1999 ? "s" : "")).send(player);
            }
            return false;
        }

        if (MMOItems.plugin.getConfig().getBoolean("permissions.abilities")
                && !player.hasPermission("mmoitems.ability." + data.getAbility().getHandler().getLowerCaseId())
                && !player.hasPermission("mmoitems.bypass.ability"))
            return false;

        if (data.hasModifier("mana") && getMana() < data.getModifier("mana")) {
            Message.NOT_ENOUGH_MANA.format(ChatColor.RED).send(player);
            return false;
        }

        if (data.hasModifier("stamina") && getStamina() < data.getModifier("stamina")) {
            Message.NOT_ENOUGH_STAMINA.format(ChatColor.RED).send(player);
            return false;
        }

        return true;
    }
}
