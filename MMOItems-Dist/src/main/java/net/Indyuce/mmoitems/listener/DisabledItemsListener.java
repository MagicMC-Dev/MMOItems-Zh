package net.Indyuce.mmoitems.listener;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.damage.MeleeAttackMetadata;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.util.MMOUtils;
import org.bukkit.GameMode;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * mmoitems
 * 13/03/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class DisabledItemsListener implements Listener {

    private final MMOItems plugin;

    public DisabledItemsListener(MMOItems plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void rightClickEffects(PlayerInteractEvent event) {
        if (!event.hasItem())
            return;
        NBTItem item = MythicLib.plugin.getVersion().getWrapper().getNBTItem(event.getItem());
        if (MMOUtils.hasBeenRemoved(item))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void meleeAttacks(PlayerAttackEvent event) {
        if (!(event.getAttack() instanceof MeleeAttackMetadata))
            return;
        ItemStack weaponUsed = event.getPlayer().getInventory().getItem(((MeleeAttackMetadata) event.getAttack()).getHand().toBukkit());
        NBTItem item = MythicLib.plugin.getVersion().getWrapper().getNBTItem(weaponUsed);
        if (MMOUtils.hasBeenRemoved(item))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void specialToolAbilities(BlockBreakEvent event) {
        final Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE)
            return;

        final NBTItem item = MythicLib.plugin.getVersion().getWrapper().getNBTItem(player.getInventory().getItemInMainHand());
        if (MMOUtils.hasBeenRemoved(item))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void rightClickWeaponInteractions(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (!(event.getRightClicked() instanceof LivingEntity))
            return;

        NBTItem item = MythicLib.plugin.getVersion().getWrapper().getNBTItem(player.getInventory().getItemInMainHand());
        if (MMOUtils.hasBeenRemoved(item))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void gemStonesAndItemStacks(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (event.getAction() != InventoryAction.SWAP_WITH_CURSOR)
            return;

        NBTItem item = MythicLib.plugin.getVersion().getWrapper().getNBTItem(event.getCursor());
        if (MMOUtils.hasBeenRemoved(item))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void handleCustomBows(EntityShootBowEvent event) {
        if (!(event.getProjectile() instanceof Arrow) || !(event.getEntity() instanceof Player))
            return;

        final NBTItem item = NBTItem.get(event.getBow());
        if (MMOUtils.hasBeenRemoved(item))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void handleVanillaEatenConsumables(PlayerItemConsumeEvent event) {
        NBTItem item = MythicLib.plugin.getVersion().getWrapper().getNBTItem(event.getItem());
        if (MMOUtils.hasBeenRemoved(item))
            event.setCancelled(true);
    }
}
