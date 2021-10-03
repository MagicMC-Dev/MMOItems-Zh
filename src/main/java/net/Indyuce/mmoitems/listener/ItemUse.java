package net.Indyuce.mmoitems.listener;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.stat.StatMap;
import io.lumine.mythic.lib.comp.target.InteractionType;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.TypeSet;
import net.Indyuce.mmoitems.api.interaction.*;
import net.Indyuce.mmoitems.api.interaction.weapon.Gauntlet;
import net.Indyuce.mmoitems.api.interaction.weapon.Weapon;
import net.Indyuce.mmoitems.api.interaction.weapon.untargeted.Staff;
import net.Indyuce.mmoitems.api.interaction.weapon.untargeted.UntargetedWeapon;
import net.Indyuce.mmoitems.api.interaction.weapon.untargeted.UntargetedWeapon.WeaponType;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.message.Message;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;

public class ItemUse implements Listener {
    private static final DecimalFormat DIGIT = new DecimalFormat("0.#");

    @EventHandler
    public void rightClickEffects(PlayerInteractEvent event) {
        if (!event.hasItem())
            // || event.getHand() != EquipmentSlot.HAND
            return;

        NBTItem item = MythicLib.plugin.getVersion().getWrapper().getNBTItem(event.getItem());
        if (!item.hasType())
            return;

        /**
         * Some consumables must be fully eaten through the vanilla eating
         * animation and are handled there {@link #handleVanillaEatenConsumables(PlayerItemConsumeEvent)}
         */
        Player player = event.getPlayer();
        UseItem useItem = UseItem.getItem(player, item, item.getType());
        if (useItem instanceof Consumable && ((Consumable) useItem).hasVanillaEating())
            return;

        // (BUG FIX) Cancel the event to prevent things like shield blocking
        if (!useItem.checkItemRequirements()) {
            event.setCancelled(true);
            return;
        }

        // Commands & consummables
        if (event.getAction().name().contains("RIGHT_CLICK")) {
            if (useItem.getPlayerData().isOnCooldown(useItem.getMMOItem())) {
                Message.ITEM_ON_COOLDOWN
                        .format(ChatColor.RED, "#left#", DIGIT.format(useItem.getPlayerData().getItemCooldown(useItem.getMMOItem())))
                        .send(player);
                event.setCancelled(true);
                return;
            }

            if (useItem instanceof Consumable) {
                event.setCancelled(true);
                Consumable.ConsumableConsumeResult result = ((Consumable) useItem).useOnPlayer();
                if (result == Consumable.ConsumableConsumeResult.CANCEL)
                    return;

                else if (result == Consumable.ConsumableConsumeResult.CONSUME)
                    event.getItem().setAmount(event.getItem().getAmount() - 1);
            }

            useItem.getPlayerData().applyItemCooldown(useItem.getMMOItem(), useItem.getNBTItem().getStat("ITEM_COOLDOWN"));
            useItem.executeCommands();
        }

        if (useItem instanceof UntargetedWeapon) {
            UntargetedWeapon weapon = (UntargetedWeapon) useItem;
            if ((event.getAction().name().contains("RIGHT_CLICK") && weapon.getWeaponType() == WeaponType.RIGHT_CLICK)
                    || (event.getAction().name().contains("LEFT_CLICK") && weapon.getWeaponType() == WeaponType.LEFT_CLICK))
                weapon.untargetedAttack(event.getHand());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void meleeAttacks(EntityDamageByEntityEvent event) {

        /*
         * Citizens and Sentinels NPC support; damage = 0 check to ignore safety
         * checks; check for entity attack
         */
        if (event.getDamage() == 0 || event.getCause() != DamageCause.ENTITY_ATTACK || !(event.getEntity() instanceof LivingEntity)
                || !(event.getDamager() instanceof Player) || event.getEntity().hasMetadata("NPC") || event.getDamager().hasMetadata("NPC"))
            return;

        // Custom damage check
        LivingEntity target = (LivingEntity) event.getEntity();
        if (MythicLib.plugin.getDamage().findInfo(target) != null)
            return;

        /*
         * Must apply attack conditions before apply any effects. the event must
         * be cancelled before anything is applied
         */
        Player player = (Player) event.getDamager();
        PlayerData playerData = PlayerData.get(player);
        NBTItem item = MythicLib.plugin.getVersion().getWrapper().getNBTItem(player.getInventory().getItemInMainHand());
        ItemAttackMetadata attackMeta = null;

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

            if (!weapon.handleTargetedAttack(attackMeta = getAttack(playerData, event), target).isSuccessful()) {
                event.setCancelled(true);
                return;
            }
        }

        // Cast on-hit abilities and add the extra damage to the damage event
        (attackMeta == null ? attackMeta = getAttack(playerData, event) : attackMeta).applyEffects(item, target);

        // Finally update Bukkit event
        event.setDamage(attackMeta.getDamage().getDamage());
    }

    private ItemAttackMetadata getAttack(PlayerData playerData, EntityDamageByEntityEvent event) {
        StatMap.CachedStatMap cachedStatMap = playerData.getMMOPlayerData().getStatMap().cache(EquipmentSlot.MAIN_HAND);
        return new ItemAttackMetadata(new DamageMetadata(event.getDamage(), DamageType.WEAPON, DamageType.PHYSICAL), cachedStatMap);
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
        if (!MMOUtils.canTarget(player, target, InteractionType.OFFENSE_ACTION))
            return;

        UseItem weapon = UseItem.getItem(player, item, item.getType());
        if (!weapon.checkItemRequirements())
            return;

        // Special staff attack
        if (weapon instanceof Staff)
            ((Staff) weapon).specialAttack(target);

        // Special gauntlet attack
        if (weapon instanceof Gauntlet)
            ((Gauntlet) weapon).specialAttack(target);
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

    @EventHandler
    public void handleCustomBows(EntityShootBowEvent event) {
        if (!(event.getProjectile() instanceof Arrow) || !(event.getEntity() instanceof Player))
            return;

        NBTItem item = NBTItem.get(event.getBow());
        if (!item.hasType())
            return;
        Type type = Type.get(item.getType());


        PlayerData playerData = PlayerData.get((Player) event.getEntity());
        if (type != null) {
            Weapon weapon = new Weapon(playerData, item);
            if (!weapon.checkItemRequirements() || !weapon.applyWeaponCosts()) {
                event.setCancelled(true);
                return;
            }

			/*if (!checkDualWield((Player) event.getEntity(), item, bowSlot)) {
				event.setCancelled(true);
				return;
			}*/
        }

        // Have to get hand manually because 1.15 and below does not have event.getHand()
        ItemStack itemInMainHand = playerData.getPlayer().getInventory().getItemInMainHand();
        EquipmentSlot bowSlot = (itemInMainHand.isSimilar(event.getBow())) ? EquipmentSlot.MAIN_HAND : EquipmentSlot.OFF_HAND;

        Arrow arrow = (Arrow) event.getProjectile();
        if (item.getStat("ARROW_VELOCITY") > 0)
            arrow.setVelocity(arrow.getVelocity().multiply(item.getStat("ARROW_VELOCITY")));
        MMOItems.plugin.getEntities().registerCustomProjectile(item, playerData.getStats().newTemporary(bowSlot), event.getProjectile(), type != null,
                event.getForce());
    }

    /**
     * Consumables which can be eaten using the
     * vanilla eating animation are handled here.
     */
    @EventHandler
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

            if (useItem.getPlayerData().isOnCooldown(useItem.getMMOItem())) {
                Message.ITEM_ON_COOLDOWN
                        .format(ChatColor.RED, "#left#", DIGIT.format(useItem.getPlayerData().getItemCooldown(useItem.getMMOItem())))
                        .send(player);
                event.setCancelled(true);
                return;
            }

            Consumable.ConsumableConsumeResult result = ((Consumable) useItem).useOnPlayer();

            // No effects are applied and not consumed
            if (result == Consumable.ConsumableConsumeResult.CANCEL) {
                event.setCancelled(true);
                return;
            }

            // Item is not consumed but its effects are applied anyways
            if (result == Consumable.ConsumableConsumeResult.NOT_CONSUME)
                event.setCancelled(true);

            useItem.getPlayerData().applyItemCooldown(useItem.getMMOItem(), useItem.getNBTItem().getStat("ITEM_COOLDOWN"));
            useItem.executeCommands();
        }
    }
}
