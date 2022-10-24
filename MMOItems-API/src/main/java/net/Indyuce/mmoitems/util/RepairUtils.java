package net.Indyuce.mmoitems.util;

import io.lumine.mythic.lib.api.item.NBTItem;
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
     * Repairs a vanilla item.
     * It will not work with MMOItems items.
     *
     * @param playerData       The player data.
     * @param target           The target item.
     * @param consumable       The consumable item.
     * @param repairPercentage The repair percentage. (set to -1 to disable)
     * @param repairUses       The repair uses. (set to -1 to disable)
     * @return True if the item was repaired.
     */
    public static boolean repairVanillaItem(@NotNull PlayerData playerData, @NotNull NBTItem target, @NotNull Consumable consumable, double repairPercentage, int repairUses) {
        final Player player = playerData.getPlayer();
        final boolean percentage = repairPercentage > 0;
        if (target.getBoolean("Unbreakable")
                || !target.getItem().hasItemMeta()
                || !(target.getItem().getItemMeta() instanceof Damageable meta)
                || ((Damageable) target.getItem().getItemMeta()).getDamage() <= 0)
            return false;
        RepairItemEvent called = percentage ? new RepairItemEvent(playerData, consumable.getMMOItem(), target, repairPercentage) : new RepairItemEvent(playerData, consumable.getMMOItem(), target, repairUses);
        Bukkit.getPluginManager().callEvent(called);
        if (called.isCancelled())
            return false;
        int repaired = percentage ? (int) ((repairPercentage / 100) * target.getItem().getType().getMaxDurability()) : repairUses;
        meta.setDamage(Math.max(0, meta.getDamage() - repaired));
        target.getItem().setItemMeta(meta);
        Message.REPAIRED_ITEM.format(ChatColor.YELLOW,
                        "#item#",
                        MMOUtils.getDisplayName(target.getItem()),
                        "#amount#",
                        String.valueOf(repaired))
                .send(player);
        CustomSoundListener.playConsumableSound(consumable.getItem(), player);
        return true;
    }
}
