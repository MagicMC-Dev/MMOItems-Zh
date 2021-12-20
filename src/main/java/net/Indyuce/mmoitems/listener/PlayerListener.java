package net.Indyuce.mmoitems.listener;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.utils.Schedulers;
import io.lumine.mythic.utils.events.extra.ArmorEquipEvent;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.SoulboundInfo;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.interaction.util.InteractItem;
import net.Indyuce.mmoitems.api.interaction.weapon.Weapon;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PlayerListener implements Listener {
    private final Map<Player, ArrayList<ItemStack>> deathItems = new HashMap<>();

    @EventHandler(priority = EventPriority.NORMAL)
    public void loadPlayerData(PlayerJoinEvent event) {
        MMOItems.plugin.getRecipes().refreshRecipeBook(event.getPlayer());
        PlayerData.load(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void savePlayerData(PlayerQuitEvent event) {
        PlayerData.get(event.getPlayer()).save();
    }

    /*
     * Prevent players from dropping items which are bound to them with a
     * soulbound. Items are cached inside a map waiting for the player to
     * respawn. If he does not respawn the items are dropped on the ground, this
     * way there don't get lost
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent event) {
        if (event.getKeepInventory() || !MMOItems.plugin.getLanguage().keepSoulboundOnDeath)
            return;

        Player player = event.getEntity();
        SoulboundInfo soulboundInfo = new SoulboundInfo(player);

        Iterator<ItemStack> iterator = event.getDrops().iterator();
        while (iterator.hasNext()) {
            ItemStack item = iterator.next();
            NBTItem nbt = NBTItem.get(item);

            if (nbt.hasTag("MMOITEMS_DISABLE_DEATH_DROP") && nbt.getBoolean("MMOITEMS_DISABLE_DEATH_DROP")) {
                iterator.remove();
                if (!deathItems.containsKey(player))
                    deathItems.put(player, new ArrayList<>());

                deathItems.get(player).add(item);
            }

            /*
             * not a perfect check but it's very sufficient and so we avoid
             * using a JsonParser followed by map checkups in the SoulboundData
             * constructor
             */
            else if (nbt.hasTag("MMOITEMS_SOULBOUND") && nbt.getString("MMOITEMS_SOULBOUND").contains(player.getUniqueId().toString())) {
                iterator.remove();
                soulboundInfo.add(item);
            }
        }

        if (soulboundInfo.hasItems())
            soulboundInfo.setup();
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (MMOItems.plugin.getLanguage().keepSoulboundOnDeath)
            SoulboundInfo.read(player);

        if (deathItems.containsKey(player)) {
            Schedulers.sync().runLater(() -> {
                player.getInventory().addItem(deathItems.get(player).toArray(new ItemStack[0]));
                deathItems.remove(player);
            }, 10);
        }
    }

    @EventHandler
    public void onArmorEquip(ArmorEquipEvent event) {
        Player player = event.getPlayer();
        if (!PlayerData.has(player))
            return;

        NBTItem item = NBTItem.get(event.getNewArmorPiece());
        if (!PlayerData.get(player).getRPG().canUse(item, true))
            event.setCancelled(true);
    }

    /**
     * This handler listens to ALL trident shootings, including both
     * custom tridents from MMOItems AND vanilla tridents, since MMOItems
     * needs to apply on-hit effects like crits, elemental damage... even
     * if the player is using a vanilla trident.
     * <p>
     * Fixing commit 6cf6f741
     */
    @EventHandler(ignoreCancelled = true)
    public void registerTridents(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof Trident) || !(event.getEntity().getShooter() instanceof Player))
            return;

        InteractItem item = new InteractItem((Player) event.getEntity().getShooter(), Material.TRIDENT);
        if (!item.hasItem())
            return;

        NBTItem nbtItem = MythicLib.plugin.getVersion().getWrapper().getNBTItem(item.getItem());
        Type type = Type.get(nbtItem.getType());
        PlayerData playerData = PlayerData.get((Player) event.getEntity().getShooter());

        if (type != null) {
            Weapon weapon = new Weapon(playerData, nbtItem);
            if (!weapon.checkItemRequirements() || !weapon.applyWeaponCosts()) {
                event.setCancelled(true);
                return;
            }
        }

        MMOItems.plugin.getEntities().registerCustomProjectile(nbtItem, playerData.getStats().newTemporary(EquipmentSlot.fromBukkit(item.getSlot())), event.getEntity(), type != null);
    }

    /**
     * Fixes an issue where quickly swapping items in hand just
     * does not update the player's inventory which can make the
     * player cast abilities or attacks with not the correct stats
     *
     * @deprecated This does cost some performance and that update
     *         method NEEDS some improvement in the future
     */
    @Deprecated
    @EventHandler
    public void registerInventoryUpdates1(PlayerSwapHandItemsEvent event) {
        PlayerData.get(event.getPlayer()).getInventory().scheduleUpdate();
    }

    /**
     * Fixes an issue where quickly swapping items in hand just
     * does not update the player's inventory which can make the
     * player cast abilities or attacks with not the correct stats
     *
     * @deprecated This does cost some performance and that update
     *         method NEEDS some improvement in the future
     */
    @Deprecated
    @EventHandler
    public void registerInventoryUpdates2(PlayerItemHeldEvent event) {
        PlayerData.get(event.getPlayer()).getInventory().scheduleUpdate();
    }
}
