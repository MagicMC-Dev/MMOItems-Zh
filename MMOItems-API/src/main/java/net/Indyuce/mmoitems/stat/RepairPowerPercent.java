package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.util.MMOUtils;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.event.item.RepairItemEvent;
import net.Indyuce.mmoitems.api.interaction.Consumable;
import net.Indyuce.mmoitems.api.interaction.util.DurabilityItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.listener.CustomSoundListener;
import net.Indyuce.mmoitems.stat.type.ConsumableItemInteraction;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.util.RepairUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RepairPowerPercent extends DoubleStat implements ConsumableItemInteraction {
    public RepairPowerPercent() {
        super("REPAIR_PERCENT", Material.DAMAGED_ANVIL, "Repair Percentage",
                new String[] { "The percentage of total durability to repair", "When dropped onto an item." },
                new String[]{"consumable"});
    }

    @Override
    public boolean handleConsumableEffect(@NotNull InventoryClickEvent event, @NotNull PlayerData playerData, @NotNull Consumable consumable, @NotNull NBTItem target, @Nullable Type targetType) {
        double repairPower = consumable.getNBTItem().getStat(ItemStats.REPAIR_PERCENT.getId());
        if (repairPower <= 0)
            return false;

        // custom durability
        Player player = playerData.getPlayer();

        final String type = "MMOITEMS_REPAIR_TYPE";
        if((target.hasTag(type) || consumable.getNBTItem().hasTag(type)) &&
                !target.getString(type).equals(consumable.getNBTItem().getString(type))) {
            Message.UNABLE_TO_REPAIR.format(ChatColor.RED, "#item#", MMOUtils.getDisplayName(target.getItem())).send(player);
            player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1.5f);
            return false;
        }

        if (target.hasTag("MMOITEMS_DURABILITY")) {
            RepairItemEvent called = new RepairItemEvent(playerData, consumable.getMMOItem(), target, repairPower);
            Bukkit.getPluginManager().callEvent(called);
            if (called.isCancelled())
                return false;

            DurabilityItem durItem = new DurabilityItem(player, target);
            if (durItem.getDurability() < durItem.getMaxDurability()) {
                target.getItem().setItemMeta(durItem.addDurability((int) (durItem.getMaxDurability()*called.getRepairedPercent())).toItem().getItemMeta());
                Message.REPAIRED_ITEM
                        .format(ChatColor.YELLOW, "#item#", MMOUtils.getDisplayName(target.getItem()), "#amount#", "" + called.getRepairedPercent())
                        .send(player);
                CustomSoundListener.playConsumableSound(consumable.getItem(), player);
            }
            return true;
        }

        // vanilla durability
        return RepairUtils.repairPower(playerData, target, consumable, repairPower);
    }
}
