package net.Indyuce.mmoitems.api.event;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.player.PlayerMetadata;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called when a MMOItems arrow or trident entity is fired.
 * <br><br>
 * Contains information even on the temporary stats the player had when
 * they executed the action, if that matters for some reason...
 * <br><br>
 * Note that this event fires before the projectile actually registers,
 * be mindful of changes made to the temporary stats because they will
 * affect the projectile.
 */
public class MMOItemsProjectileFireEvent extends Event {

    /**
     * @return The item the player used to fire this arrow.
     */
    @NotNull public NBTItem getSourceItem() { return sourceItem; }
    @NotNull NBTItem sourceItem;
    /**
     * Note that you must use {@link #setFinalDamage(double)} to modify the attack
     * damage of the projectile rather than editing the attack damage of the player
     * in here, as it will get overwritten.
     *
     * @return The stats the player had at the moment of firing the projectile.
     */
    @NotNull public PlayerMetadata getPlayerStatsSnapshot() { return playerStatsSnapshot; }
    @NotNull PlayerMetadata playerStatsSnapshot;
    /**
     * @return The projectile entity that was fired, arrow or trident.
     */
    @NotNull public Entity getProjectile() { return projectile; }
    @NotNull Entity projectile;
    /**
     * @return The damage this projectile will deal
     */
    public double getFinalDamage() { return finalDamage; }
    double finalDamage;
    /**
     * @param damage The damage this projectile will deal
     */
    public void setFinalDamage(double damage) { finalDamage = damage; }
    /**
     * @return The original damage amount
     */
    public double getDamageMultiplicator() { return damageMultiplicator; }
    double damageMultiplicator;
    /**
     * @return The original damage amount
     */
    public double getOriginalDamage() { return originalDamage; }
    double originalDamage;
    /**
     * @return The kinds of damage this projectile will deal, what it will scale with.
     */
    @NotNull public DamageType[] getDamageTypes() { return damageTypes; }
    @NotNull DamageType[] damageTypes = { DamageType.PROJECTILE, DamageType.PHYSICAL, DamageType.WEAPON };
    /**
     * @param types The kinds of damage this projectile will deal, what it will scale with.
     */
    public void setDamageTypes(@NotNull DamageType[] types) { damageTypes = types; }
    /**
     * @return The event that caused this projectile to be fired. Honestly, only for informational purposes of whatever listening API.
     */
    @Nullable public EntityShootBowEvent getEvent() { return event; }
    @Nullable EntityShootBowEvent event;

    public MMOItemsProjectileFireEvent(@NotNull PlayerMetadata player, @NotNull Entity projectile, @NotNull NBTItem item, @Nullable EntityShootBowEvent event, double originalDamage, double damageMultiplicator) {
        playerStatsSnapshot = player;
        this.projectile = projectile;
        sourceItem = item;
        this.originalDamage = originalDamage;
        this.damageMultiplicator= damageMultiplicator;
        finalDamage = originalDamage * damageMultiplicator;
        this.event = event;
    }

    //region Event Standard
    private static final HandlerList handlers = new HandlerList();
    @NotNull
    @Override public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
    //endregion
}
