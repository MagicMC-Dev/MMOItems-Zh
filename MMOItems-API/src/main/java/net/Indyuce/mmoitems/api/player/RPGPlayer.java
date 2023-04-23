package net.Indyuce.mmoitems.api.player;

import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.stat.type.ItemRestriction;
import net.Indyuce.mmoitems.util.MMOUtils;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Interface class between RPG core plugins like
 * Heroes, MythicCore, SkillAPI, MMOCore and MMOItems
 *
 * @author Jules
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
    public boolean canUse(@NotNull NBTItem item, boolean message, boolean allowDynamic) {

        // Unidentification
        if (item.hasTag("MMOITEMS_UNIDENTIFIED_ITEM")) {
            if (message) {
                Message.UNIDENTIFIED_ITEM.format(ChatColor.RED).send(player.getPlayer());
                player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1.5f);
            }
            return false;
        }

        // Item has been disabled
        if (MMOItems.plugin.getLanguage().disableRemovedItems && MMOUtils.hasBeenRemoved(item)) return false;

        // Stat-based requirements
        for (ItemRestriction condition : MMOItems.plugin.getStats().getItemRestrictionStats())
            if (!condition.isDynamic() || !allowDynamic) if (!condition.canUse(this, item, message)) return false;

        return true;
    }
}
