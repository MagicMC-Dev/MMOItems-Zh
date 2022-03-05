package net.Indyuce.mmoitems.api.event.item;

import net.Indyuce.mmoitems.api.event.PlayerDataEvent;
import net.Indyuce.mmoitems.api.interaction.UseItem;
import net.Indyuce.mmoitems.api.interaction.weapon.Weapon;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class SpecialWeaponAttackEvent extends PlayerDataEvent implements Cancellable {
    private final Weapon weapon;
    private final LivingEntity target;

    private boolean cancelled;

    private static final HandlerList handlers = new HandlerList();

    /**
     * Called when performing a special weapon attack using a staff or gauntlet
     *
     * @param who    Player attacking
     * @param weapon Weapon being used
     * @param target Target
     */
    public SpecialWeaponAttackEvent(@NotNull PlayerData who, @NotNull Weapon weapon, @NotNull LivingEntity target) {
        super(who);

        this.weapon = weapon;
        this.target = target;
    }

    /**
     * @return The weapon used by the player that fired this event
     */
    @NotNull
    public Weapon getWeapon() {
        return weapon;
    }

    /**
     * @return The entity that is being hit by this special attack
     */
    @NotNull
    public LivingEntity getTarget() {
        return target;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
