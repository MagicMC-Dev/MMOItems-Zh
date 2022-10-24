package net.Indyuce.mmoitems.util;

import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.MMOUtils;
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

    public static boolean repairPower(@NotNull PlayerData playerData, @NotNull NBTItem target, @NotNull Consumable consumable, double repairPower) {
        final Player player = playerData.getPlayer();
        if (!target.getBoolean("Unbreakable") && target.getItem().hasItemMeta() && target.getItem().getItemMeta() instanceof Damageable
                && ((Damageable) target.getItem().getItemMeta()).getDamage() > 0) {
            RepairItemEvent called = new RepairItemEvent(playerData, consumable.getMMOItem(), target, repairPower);
            Bukkit.getPluginManager().callEvent(called);
            if (called.isCancelled())
                return false;

            ItemMeta meta = target.getItem().getItemMeta();
            ((Damageable) meta).setDamage(Math.max(0, ((Damageable) meta).getDamage() - called.getRepaired()));
            target.getItem().setItemMeta(meta);
            Message.REPAIRED_ITEM.format(ChatColor.YELLOW,
                            "#item#",
                            MMOUtils.getDisplayName(target.getItem()),
                            "#amount#",
                            String.valueOf(called.getRepaired() == -1 ? called.getRepairedPercent() : called.getRepaired()))
                    .send(player);
            CustomSoundListener.playConsumableSound(consumable.getItem(), player);
            return true;
        }
        return false;
    }
}
