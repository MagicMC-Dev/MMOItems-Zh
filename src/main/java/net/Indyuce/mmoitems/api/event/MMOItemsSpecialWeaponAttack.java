package net.Indyuce.mmoitems.api.event;

import net.Indyuce.mmoitems.api.interaction.UseItem;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MMOItemsSpecialWeaponAttack extends PlayerEvent implements Cancellable {

    /**
     * @return The weapon used by the player that fired this event
     */
    @NotNull public UseItem getWeapon() { return weapon; }
    @NotNull final UseItem weapon;

    /**
     * @return The entity that is being hit by this special attack
     */
    @Nullable public LivingEntity getTarget() { return target; }
    @Nullable final LivingEntity target;

    public MMOItemsSpecialWeaponAttack(@NotNull Player who, @NotNull UseItem weapon, @Nullable LivingEntity target) {
        super(who);
        this.weapon = weapon;
        this.target = target;
    }

    //region Event Standard
    private static final HandlerList handlers = new HandlerList();
    @NotNull @Override public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
    //endregion

    //region Cancellable Standard
    boolean cancelled;
    @Override public boolean isCancelled() {
        return cancelled;
    }
    @Override public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }
    //endregion
}
