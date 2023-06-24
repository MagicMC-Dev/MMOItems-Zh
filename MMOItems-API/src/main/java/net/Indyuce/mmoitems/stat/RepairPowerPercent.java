package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.api.CustomSound;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.interaction.Consumable;
import net.Indyuce.mmoitems.api.interaction.util.DurabilityItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.listener.CustomSoundListener;
import net.Indyuce.mmoitems.stat.type.ConsumableItemInteraction;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.util.MMOUtils;
import net.Indyuce.mmoitems.util.RepairUtils;
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
                new String[]{"The percentage of total durability to repair", "When dropped onto an item."},
                new String[]{"consumable"});
    }

    private static final String REPAIR_TYPE_TAG = "MMOITEMS_REPAIR_TYPE";

    @Override
    public boolean handleConsumableEffect(@NotNull InventoryClickEvent event, @NotNull PlayerData playerData, @NotNull Consumable consumable, @NotNull NBTItem target, @Nullable Type targetType) {
        final double repairPower = consumable.getNBTItem().getStat(ItemStats.REPAIR_PERCENT.getId());
        if (repairPower <= 0)
            return false;

        // Check repair reference
        final Player player = playerData.getPlayer();
        final @Nullable String repairType1 = consumable.getNBTItem().getString(REPAIR_TYPE_TAG);
        final @Nullable String repairType2 = target.getString(REPAIR_TYPE_TAG);
        if (!MMOUtils.checkReference(repairType1, repairType2)) {
            Message.UNABLE_TO_REPAIR.format(ChatColor.RED, "#item#", MMOUtils.getDisplayName(target.getItem())).send(player);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1.5f);
            return false;
        }

        final boolean customWeapon = target.hasTag("MMOITEMS_DURABILITY");
        final double maxDurability = customWeapon ? target.getDouble("MMOITEMS_MAX_DURABILITY") : target.getItem().getType().getMaxDurability();
        final int repairAmount = (int) (repairPower * maxDurability / 100);

        // Custom durability
        if (customWeapon) {

            final DurabilityItem durItem = new DurabilityItem(player, target);
            if (durItem.getDurability() < durItem.getMaxDurability()) {
                target.getItem().setItemMeta(durItem.addDurability(repairAmount).toItem().getItemMeta());
                Message.REPAIRED_ITEM
                        .format(ChatColor.YELLOW, "#item#", MMOUtils.getDisplayName(target.getItem()), "#amount#", String.valueOf(repairAmount))
                        .send(player);
                CustomSoundListener.playSound(consumable.getItem(), CustomSound.ON_CONSUME, player);
            }
            return true;
        }

        // vanilla durability
        return RepairUtils.repairVanillaItem(playerData, target, consumable, repairAmount);
    }
}
