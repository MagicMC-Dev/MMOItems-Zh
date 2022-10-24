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
import org.bukkit.inventory.meta.ItemMeta;
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

    public static boolean repairPower(@NotNull PlayerData playerData, @NotNull NBTItem target, @NotNull Consumable consumable, double repairPercentage, int repairUses) {
        final Player player = playerData.getPlayer();
        final boolean percentage = repairPercentage > 0;
        if (!target.getBoolean("Unbreakable")
                && target.getItem().hasItemMeta()
                && target.getItem().getItemMeta() instanceof Damageable
                && ((Damageable) target.getItem().getItemMeta()).getDamage() > 0) {
            RepairItemEvent called = percentage ? new RepairItemEvent(playerData, consumable.getMMOItem(), target, repairPercentage) : new RepairItemEvent(playerData, consumable.getMMOItem(), target, repairUses);
            Bukkit.getPluginManager().callEvent(called);
            if (called.isCancelled())
                return false;
            int uses = percentage ? (int) (target.getItem().getType().getMaxDurability() * (repairPercentage / 100)) : repairUses;

            ItemMeta meta = target.getItem().getItemMeta();
            ((Damageable) meta).setDamage(Math.max(0, ((Damageable) meta).getDamage() - uses));
            target.getItem().setItemMeta(meta);
            Message.REPAIRED_ITEM.format(ChatColor.YELLOW,
                            "#item#",
                            MMOUtils.getDisplayName(target.getItem()),
                            "#amount#",
                            String.valueOf(uses))
                    .send(player);
            CustomSoundListener.playConsumableSound(consumable.getItem(), player);
            return true;
        }
        return false;
    }
}
