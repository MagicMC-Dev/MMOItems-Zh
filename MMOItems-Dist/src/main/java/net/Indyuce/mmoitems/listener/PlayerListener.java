package net.Indyuce.mmoitems.listener;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.api.event.SynchronizedDataLoadEvent;
import io.lumine.mythic.lib.api.event.armorequip.ArmorEquipEvent;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.damage.ProjectileAttackMetadata;
import io.lumine.mythic.lib.entity.ProjectileMetadata;
import io.lumine.mythic.lib.entity.ProjectileType;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.DeathItemsHandler;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.interaction.projectile.ArrowPotionEffectArrayItem;
import net.Indyuce.mmoitems.api.interaction.util.InteractItem;
import net.Indyuce.mmoitems.api.interaction.weapon.Weapon;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.DeathDowngrading;
import net.Indyuce.mmoitems.stat.data.PotionEffectData;
import net.Indyuce.mmoitems.util.MMOUtils;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class PlayerListener implements Listener {

    /**
     * If the player dies, its time to roll the death-downgrade stat!
     */
    @SuppressWarnings("InstanceofIncompatibleInterface")
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onDeathForUpgradeLoss(@NotNull PlayerDeathEvent event) {

        // Supports NPCs
        final PlayerData playerData = PlayerData.getOrNull(event.getEntity());
        if (playerData == null) return;

        // See description of DelayedDeathDowngrade child class for full explanation
        final Player player = event.getEntity();
        new DelayedDeathDowngrade(playerData, player).runTaskLater(MMOItems.plugin, 3L);
    }

    /**
     * Fixes <a href="https://gitlab.com/phoenix-dvpmt/mmocore/-/issues/545">MMOCore#545</a>
     */
    @EventHandler
    public void resolveInvWhenDataLoaded(SynchronizedDataLoadEvent event) {
        if (event.syncIsFull()) {
            final PlayerData playerData = PlayerData.get(event.getHolder().getUniqueId());
            playerData.resolveInventory(); // For safety
        }
    }

    /**
     * Prevent players from dropping items which are bound to them with a
     * Soulbound. Items are cached inside a map waiting for the player to
     * respawn. If he does not respawn the items are dropped on the ground, this
     * way there don't get lost
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void keepItemsOnDeath(PlayerDeathEvent event) {
        if (event.getKeepInventory()) return;

        final Player player = event.getEntity();
        final DeathItemsHandler soulboundInfo = new DeathItemsHandler(player);

        final Iterator<ItemStack> iterator = event.getDrops().iterator();
        while (iterator.hasNext()) {
            final ItemStack item = iterator.next();
            final NBTItem nbt = NBTItem.get(item);

            if (nbt.getBoolean("MMOITEMS_DISABLE_DEATH_DROP") || (MMOItems.plugin.getLanguage().keepSoulboundOnDeath && MMOUtils.isSoulboundTo(nbt, player))) {
                iterator.remove();
                soulboundInfo.registerItem(item);
            }
        }

        soulboundInfo.registerIfNecessary();
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        DeathItemsHandler.readAndRemove(event.getPlayer());
    }

    @EventHandler
    public void onArmorEquip(ArmorEquipEvent event) {
        if (event.getNewArmorPiece() == null)
            return;

        if (!PlayerData.get(event.getPlayer()).getRPG().canUse(NBTItem.get(event.getNewArmorPiece()), true))
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

        final InteractItem item = new InteractItem((Player) event.getEntity().getShooter(), Material.TRIDENT);
        if (!item.hasItem())
            return;

        final NBTItem nbtItem = MythicLib.plugin.getVersion().getWrapper().getNBTItem(item.getItem());
        final Type type = Type.get(nbtItem.getType());
        final PlayerData playerData = PlayerData.get((Player) event.getEntity().getShooter());

        if (type != null) {
            final Weapon weapon = new Weapon(playerData, nbtItem);
            if (!weapon.checkItemRequirements() || !weapon.checkAndApplyWeaponCosts()) {
                event.setCancelled(true);
                return;
            }

            final ProjectileMetadata proj = ProjectileMetadata.create(playerData.getMMOPlayerData(), EquipmentSlot.fromBukkit(item.getSlot()), ProjectileType.TRIDENT, event.getEntity());
            proj.setSourceItem(nbtItem);
            proj.setCustomDamage(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void registerArrowSpecialEffects(PlayerAttackEvent event) {
        if (!(event.getAttack() instanceof ProjectileAttackMetadata)) return;

        final ProjectileAttackMetadata projAttack = (ProjectileAttackMetadata) event.getAttack();
        final @Nullable ProjectileMetadata data = ProjectileMetadata.get(projAttack.getProjectile());
        if (data == null || data.getSourceItem() == null) return;

        // Apply MMOItems-specific effects
        applyPotionEffects(data, event.getEntity());
    }

    private void applyPotionEffects(ProjectileMetadata proj, LivingEntity target) {
        if (proj.getSourceItem().hasTag("MMOITEMS_ARROW_POTION_EFFECTS"))
            for (ArrowPotionEffectArrayItem entry : MythicLib.plugin.getJson().parse(proj.getSourceItem().getString("MMOITEMS_ARROW_POTION_EFFECTS"), ArrowPotionEffectArrayItem[].class))
                target.addPotionEffect(new PotionEffectData(PotionEffectType.getByName(entry.type), entry.duration, entry.level).toEffect());
    }

    /**
     * Some plugins like to interfere with dropping items when the
     * player dies, or whatever of that sort.
     * <p>
     * MMOItems would hate to dupe items because of this, as such, we wait
     * 3 ticks for those plugins to reasonably complete their operations and
     * then downgrade the items the player still has equipped.
     * <p>
     * If a plugin removes items in this time, they will be completely excluded
     * and no dupes will be caused, and if a plugin adds items, they will be
     * included and downgraded. I think that's reasonable behaviour.
     *
     * @author Gunging
     */
    private static class DelayedDeathDowngrade extends BukkitRunnable {

        final PlayerData playerData;
        final Player player;

        DelayedDeathDowngrade(@NotNull PlayerData playerData, @NotNull Player player) {
            this.player = player;
            this.playerData = playerData;
        }

        @Override
        public void run() {

            // Downgrade player's inventory
            DeathDowngrading.playerDeathDowngrade(playerData, player);
        }
    }
}
