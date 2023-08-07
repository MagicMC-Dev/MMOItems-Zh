package net.Indyuce.mmoitems.util;

import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.api.CustomSound;
import net.Indyuce.mmoitems.api.event.item.RepairItemEvent;
import net.Indyuce.mmoitems.api.interaction.Consumable;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.listener.CustomSoundListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.NotNull;

/**
 * mmoitems
 *
 * @author Roch Blondiaux
 * @date 24/10/2022
 */
public class RepairUtils {

    public RepairUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Repairs a vanilla item. This method does NOT work with
     * custom MI items and will NOT call the corresponding custom event.
     *
     * @param playerData   The player data.
     * @param target       The target item.
     * @param consumable   The consumable item.
     * @param repairAmount The repair uses.
     * @return True if the item was repaired.
     */
    public static boolean repairVanillaItem(@NotNull PlayerData playerData, @NotNull NBTItem target, @NotNull Consumable consumable, int repairAmount) {
        final Player player = playerData.getPlayer();
        if (target.getBoolean("Unbreakable")
                || !target.getItem().hasItemMeta()
                || !(target.getItem().getItemMeta() instanceof Damageable)
                || ((Damageable) target.getItem().getItemMeta()).getDamage() <= 0)
            return false;

        final RepairItemEvent called = new RepairItemEvent(playerData, consumable.getMMOItem(), target, repairAmount);
        Bukkit.getPluginManager().callEvent(called);
        if (called.isCancelled())
            return false;

        repairAmount = called.getRepaired();
        final Damageable meta = (Damageable) target.getItem().getItemMeta();
        meta.setDamage(Math.max(0, meta.getDamage() - repairAmount));
        target.getItem().setItemMeta(meta);
        Message.REPAIRED_ITEM.format(ChatColor.YELLOW,
                        "#item#", MMOUtils.getDisplayName(target.getItem()),
                        "#amount#", String.valueOf(repairAmount))
                .send(player);
        CustomSoundListener.playSound(consumable.getItem(), CustomSound.ON_CONSUME, player);
        return true;
    }
}
