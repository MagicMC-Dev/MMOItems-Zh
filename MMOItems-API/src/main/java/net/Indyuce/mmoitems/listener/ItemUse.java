package net.Indyuce.mmoitems.listener;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.comp.interaction.InteractionType;
import io.lumine.mythic.lib.damage.MeleeAttackMetadata;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.TypeSet;
import net.Indyuce.mmoitems.api.event.item.SpecialWeaponAttackEvent;
import net.Indyuce.mmoitems.api.interaction.*;
import net.Indyuce.mmoitems.api.interaction.projectile.ProjectileData;
import net.Indyuce.mmoitems.api.interaction.weapon.Gauntlet;
import net.Indyuce.mmoitems.api.interaction.weapon.Weapon;
import net.Indyuce.mmoitems.api.interaction.weapon.untargeted.Staff;
import net.Indyuce.mmoitems.api.interaction.weapon.untargeted.UntargetedWeapon;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
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

public class ItemUse implements Listener {

    @EventHandler
    public void rightClickEffects(PlayerInteractEvent event) {
        if (!event.hasItem())
            // || event.getHand() != EquipmentSlot.HAND
            return;

        NBTItem item = MythicLib.plugin.getVersion().getWrapper().getNBTItem(event.getItem());
        if (!item.hasType())
            return;

        /*
         * Some consumables must be fully eaten through the vanilla eating
         * animation and are handled there {@link #handleVanillaEatenConsumables(PlayerItemConsumeEvent)}
         */
        final Player player = event.getPlayer();
        final UseItem useItem = UseItem.getItem(player, item, item.getType());
        if (useItem instanceof Consumable && ((Consumable) useItem).hasVanillaEating())
            return;

        // (BUG FIX) Cancel the event to prevent things like shield blocking
        if (!useItem.checkItemRequirements()) {
            event.setUseItemInHand(Event.Result.DENY);
            return;
        }

        // Commands & consummables
        if (event.getAction().name().contains("RIGHT_CLICK")) {
            if (useItem.getPlayerData().getMMOPlayerData().getCooldownMap().isOnCooldown(useItem.getMMOItem())) {
                final double cd = useItem.getPlayerData().getMMOPlayerData().getCooldownMap().getCooldown(useItem.getMMOItem());
                Message.ITEM_ON_COOLDOWN
                        .format(ChatColor.RED, "#left#", MythicLib.plugin.getMMOConfig().decimal.format(cd), "#s#", cd >= 2 ? "s" : "")
                        .send(player);
                event.setUseItemInHand(Event.Result.DENY);
                return;
            }

            if (useItem instanceof Consumable) {
                event.setUseItemInHand(Event.Result.DENY);
                Consumable.ConsumableConsumeResult result = ((Consumable) useItem).useOnPlayer(event.getHand(), false);
                if (result == Consumable.ConsumableConsumeResult.CANCEL)
                    return;

                else if (result == Consumable.ConsumableConsumeResult.CONSUME)
                    event.getItem().setAmount(event.getItem().getAmount() - 1);
            }

            useItem.getPlayerData().getMMOPlayerData().getCooldownMap().applyCooldown(useItem.getMMOItem(), useItem.getNBTItem().getStat("ITEM_COOLDOWN"));
            useItem.executeCommands();
        }

        // Target free weapon attack
        if (useItem instanceof UntargetedWeapon) {
            UntargetedWeapon weapon = (UntargetedWeapon) useItem;
            if (weapon.getWeaponType().corresponds(event.getAction()))
                weapon.handleTargetFreeAttack(EquipmentSlot.fromBukkit(event.getHand()));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void meleeAttacks(PlayerAttackEvent event) {

        // Checks if it's a melee attack
        if (!(event.getAttack() instanceof MeleeAttackMetadata))
            return;

        /*
         * Must apply attack conditions before apply any effects. the event must
         * be cancelled before anything is applied
         */
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.get(player);
        ItemStack weaponUsed = player.getInventory().getItem(((MeleeAttackMetadata) event.getAttack()).getHand().toBukkit());
        NBTItem item = MythicLib.plugin.getVersion().getWrapper().getNBTItem(weaponUsed);

        if (item.hasType() && Type.get(item.getType()) != Type.BLOCK) {
            Weapon weapon = new Weapon(playerData, item);

            if (weapon.getMMOItem().getType().getItemSet() == TypeSet.RANGE) {
                event.setCancelled(true);
                return;
            }

            if (!weapon.checkItemRequirements()) {
                event.setCancelled(true);
                return;
            }

            if (!weapon.handleTargetedAttack(event.getAttack(), event.getEntity())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * Event priority set to LOW to fix an infinite-exp glitch with
     * MMOCore. MMOCore experience source listens on HIGH and must be
     * higher than this event otherwise the exp is given even if the
     * block is not broken.
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void specialToolAbilities(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (player.getGameMode() == GameMode.CREATIVE)
            return;

        NBTItem item = MythicLib.plugin.getVersion().getWrapper().getNBTItem(player.getInventory().getItemInMainHand());
        if (!item.hasType())
            return;

        Tool tool = new Tool(player, item);
        if (!tool.checkItemRequirements()) {
            event.setCancelled(true);
            return;
        }

        if (tool.miningEffects(block))
            event.setCancelled(true);
    }

    @EventHandler
    public void rightClickWeaponInteractions(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (!(event.getRightClicked() instanceof LivingEntity))
            return;

        NBTItem item = MythicLib.plugin.getVersion().getWrapper().getNBTItem(player.getInventory().getItemInMainHand());
        if (!item.hasType())
            return;

        LivingEntity target = (LivingEntity) event.getRightClicked();
        if (!UtilityMethods.canTarget(player, target, InteractionType.OFFENSE_ACTION))
            return;

        UseItem weapon = UseItem.getItem(player, item, item.getType());
        if (!weapon.checkItemRequirements())
            return;

        // Special staff attack
        if (weapon instanceof Staff) {
            SpecialWeaponAttackEvent attackEvent = new SpecialWeaponAttackEvent(weapon.getPlayerData(), (Weapon) weapon, target);
            Bukkit.getPluginManager().callEvent(attackEvent);
            if (!attackEvent.isCancelled())
                ((Staff) weapon).specialAttack(target);
        }

        // Special gauntlet attack
        if (weapon instanceof Gauntlet) {
            SpecialWeaponAttackEvent attackEvent = new SpecialWeaponAttackEvent(weapon.getPlayerData(), (Weapon) weapon, target);
            Bukkit.getPluginManager().callEvent(attackEvent);
            if (!attackEvent.isCancelled())
                ((Gauntlet) weapon).specialAttack(target);
        }
    }

    // TODO: Rewrite this with a custom 'ApplyMMOItemEvent'?
    @EventHandler
    public void gemStonesAndItemStacks(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (event.getAction() != InventoryAction.SWAP_WITH_CURSOR)
            return;

        NBTItem item = MythicLib.plugin.getVersion().getWrapper().getNBTItem(event.getCursor());
        if (!item.hasType())
            return;

        UseItem useItem = UseItem.getItem(player, item, item.getType());
        if (!useItem.checkItemRequirements())
            return;

        if (useItem instanceof ItemSkin) {
            NBTItem picked = MythicLib.plugin.getVersion().getWrapper().getNBTItem(event.getCurrentItem());
            if (!picked.hasType())
                return;

            ItemSkin.ApplyResult result = ((ItemSkin) useItem).applyOntoItem(picked, Type.get(picked.getType()));
            if (result.getType() == ItemSkin.ResultType.NONE)
                return;

            event.setCancelled(true);
            item.getItem().setAmount(item.getItem().getAmount() - 1);

            if (result.getType() == ItemSkin.ResultType.FAILURE)
                return;

            event.setCurrentItem(result.getResult());
        }

        if (useItem instanceof GemStone) {
            NBTItem picked = MythicLib.plugin.getVersion().getWrapper().getNBTItem(event.getCurrentItem());
            if (!picked.hasType())
                return;

            GemStone.ApplyResult result = ((GemStone) useItem).applyOntoItem(picked, Type.get(picked.getType()));
            if (result.getType() == GemStone.ResultType.NONE)
                return;

            event.setCancelled(true);
            item.getItem().setAmount(item.getItem().getAmount() - 1);

            if (result.getType() == GemStone.ResultType.FAILURE)
                return;

            event.setCurrentItem(result.getResult());
        }

        if (useItem instanceof Consumable && event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR)
            if (((Consumable) useItem).useOnItem(event, MythicLib.plugin.getVersion().getWrapper().getNBTItem(event.getCurrentItem()))) {
                event.setCancelled(true);
                event.getCursor().setAmount(event.getCursor().getAmount() - 1);
            }
    }

    /**
     * This handler listens to ALL bow shootings, including both
     * custom bows from MMOItems AND vanilla bows, since MMOItems needs to
     * apply on-hit effects like crits, elemental damage... even if the
     * player is using a vanilla bow.
     * <p>
     * Fixing commit 4aec1433
     */
    @EventHandler
    public void handleCustomBows(EntityShootBowEvent event) {
        if (!(event.getProjectile() instanceof AbstractArrow) || !(event.getEntity() instanceof Player))
            return;

        final NBTItem item = NBTItem.get(event.getBow());
        final Type type = Type.get(item.getType());

        if (type != null) {
            final PlayerData playerData = PlayerData.get((Player) event.getEntity());
            final Weapon weapon = new Weapon(playerData, item);
            if (!weapon.checkItemRequirements() || !weapon.checkAndApplyWeaponCosts()) {
                event.setCancelled(true);
                return;
            }

            // Have to get hand manually because 1.15 and below does not have event.getHand()
            final ItemStack itemInMainHand = playerData.getPlayer().getInventory().getItemInMainHand();
            final EquipmentSlot bowSlot = itemInMainHand.isSimilar(event.getBow()) ? EquipmentSlot.MAIN_HAND : EquipmentSlot.OFF_HAND;
            final ProjectileData projData = MMOItems.plugin.getEntities().registerCustomProjectile(item, playerData.getStats().newTemporary(bowSlot), event.getProjectile(), event.getForce());
            final AbstractArrow arrow = (AbstractArrow) event.getProjectile();

            // Apply arrow velocity
            final double arrowVelocity = projData.getShooter().getStat("ARROW_VELOCITY");
            if (arrowVelocity > 0)
                arrow.setVelocity(arrow.getVelocity().multiply(arrowVelocity));
        }
    }

    /**
     * Consumables which can be eaten using the
     * vanilla eating animation are handled here.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void handleVanillaEatenConsumables(PlayerItemConsumeEvent event) {
        NBTItem item = MythicLib.plugin.getVersion().getWrapper().getNBTItem(event.getItem());
        if (!item.hasType())
            return;

        Player player = event.getPlayer();
        UseItem useItem = UseItem.getItem(player, item, item.getType());
        if (!useItem.checkItemRequirements()) {
            event.setCancelled(true);
            return;
        }

        if (useItem instanceof Consumable) {

            if (useItem.getPlayerData().getMMOPlayerData().getCooldownMap().isOnCooldown(useItem.getMMOItem())) {
                final double cd = useItem.getPlayerData().getMMOPlayerData().getCooldownMap().getCooldown(useItem.getMMOItem());
                Message.ITEM_ON_COOLDOWN
                        .format(ChatColor.RED, "#left#", MythicLib.plugin.getMMOConfig().decimal.format(cd), "#s#", cd >= 2 ? "s" : "")
                        .send(player);
                event.setCancelled(true);
                return;
            }

            Consumable.ConsumableConsumeResult result = ((Consumable) useItem).useOnPlayer(event.getItem().equals(player.getInventory().getItemInMainHand()) ? org.bukkit.inventory.EquipmentSlot.HAND : org.bukkit.inventory.EquipmentSlot.OFF_HAND, true);

            // No effects are applied and not consumed
            if (result == Consumable.ConsumableConsumeResult.CANCEL) {
                event.setCancelled(true);
                return;
            }

            // Item is not consumed but its effects are applied anyways
            if (result == Consumable.ConsumableConsumeResult.NOT_CONSUME)
                event.setCancelled(true);

            useItem.getPlayerData().getMMOPlayerData().getCooldownMap().applyCooldown(useItem.getMMOItem(), useItem.getNBTItem().getStat("ITEM_COOLDOWN"));
            useItem.executeCommands();
        }
    }
}
