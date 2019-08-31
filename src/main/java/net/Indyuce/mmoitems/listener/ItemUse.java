package net.Indyuce.mmoitems.listener;

import java.text.DecimalFormat;

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
import org.bukkit.inventory.EquipmentSlot;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.AttackResult;
import net.Indyuce.mmoitems.api.AttackResult.DamageType;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.TypeSet;
import net.Indyuce.mmoitems.api.interaction.Consumable;
import net.Indyuce.mmoitems.api.interaction.GemStone;
import net.Indyuce.mmoitems.api.interaction.GemStone.ApplyResult;
import net.Indyuce.mmoitems.api.interaction.GemStone.ResultType;
import net.Indyuce.mmoitems.api.interaction.Tool;
import net.Indyuce.mmoitems.api.interaction.UseItem;
import net.Indyuce.mmoitems.api.interaction.weapon.Gauntlet;
import net.Indyuce.mmoitems.api.interaction.weapon.Weapon;
import net.Indyuce.mmoitems.api.interaction.weapon.untargeted.Staff;
import net.Indyuce.mmoitems.api.interaction.weapon.untargeted.UntargetedWeapon;
import net.Indyuce.mmoitems.api.interaction.weapon.untargeted.UntargetedWeapon.WeaponType;
import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.gui.AdvancedWorkbench;
import net.Indyuce.mmoitems.stat.type.ItemStat;

public class ItemUse implements Listener {
	private static final DecimalFormat digit = new DecimalFormat("0.#");

	@EventHandler
	public void a(PlayerInteractEvent event) {
		if (!event.hasItem() || event.getHand() != EquipmentSlot.HAND)
			return;

		NBTItem item = MMOItems.plugin.getNMS().getNBTItem(event.getItem());
		if (!item.hasType())
			return;

		/*
		 * some consumables cannot be used by right clicking since they need to
		 * be eaten by waiting the vanilla eating animation in order to be
		 * successfully consumed
		 */
		Player player = event.getPlayer();
		UseItem useItem = UseItem.getItem(player, item, item.getType());
		if (useItem instanceof Consumable && ((Consumable) useItem).hasVanillaEating())
			return;

		if (!useItem.canBeUsed())
			return;

		// commands & consummables
		if (event.getAction().name().contains("RIGHT_CLICK")) {
			if (!useItem.getPlayerData().canUseItem(useItem.getMMOItem().getId())) {
				Message.ITEM_ON_COOLDOWN.format(ChatColor.RED, "#left#", digit.format(useItem.getPlayerData().getItemCooldown(useItem.getMMOItem().getId()))).send(player);
				return;
			}

			useItem.getPlayerData().applyItemCooldown(useItem.getMMOItem().getId(), useItem.getNBTItem().getStat(ItemStat.ITEM_COOLDOWN));
			useItem.executeCommands();

			if (useItem instanceof Consumable) {
				event.setCancelled(true);
				if (((Consumable) useItem).useWithoutItem(true))
					event.getItem().setAmount(event.getItem().getAmount() - 1);
			}
		}

		if (useItem instanceof UntargetedWeapon) {
			UntargetedWeapon weapon = (UntargetedWeapon) useItem;
			if ((event.getAction().name().contains("RIGHT_CLICK") && weapon.getWeaponType() == WeaponType.RIGHT_CLICK) || (event.getAction().name().contains("LEFT_CLICK") && weapon.getWeaponType() == WeaponType.LEFT_CLICK))
				weapon.untargetedAttack(EquipmentSlot.HAND);
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void b(EntityDamageByEntityEvent event) {

		// check for npc
		// safety checks
		if (event.getEntity().hasMetadata("NPC") || !(event.getDamager() instanceof Player) || !(event.getEntity() instanceof LivingEntity) || event.getDamage() == 0 || event.getCause() != DamageCause.ENTITY_ATTACK)
			return;

		// custom damage check
		LivingEntity target = (LivingEntity) event.getEntity();
		if (MMOItems.plugin.getDamage().findInfo(target) != null)
			return;

		Player player = (Player) event.getDamager();
		TemporaryStats stats = null;

		/*
		 * must apply attack conditions before apply any effects. the event must
		 * be cancelled before anything is applied
		 */
		PlayerData playerData = PlayerData.get(player);
		NBTItem item = MMOItems.plugin.getNMS().getNBTItem(player.getInventory().getItemInMainHand());
		AttackResult result = new AttackResult(event.getDamage(), DamageType.WEAPON, DamageType.PHYSICAL);
		if (item.hasType()) {
			Weapon weapon = new Weapon(playerData, item, item.getType());

			if (weapon.getMMOItem().getType().getItemSet() == TypeSet.RANGE) {
				event.setCancelled(true);
				return;
			}

			if (!weapon.canBeUsed()) {
				event.setCancelled(true);
				return;
			}

			weapon.targetedAttack(stats = playerData.getStats().newTemporary(), target, EquipmentSlot.HAND, result.setSuccessful(true));
			if (!result.isSuccessful()) {
				event.setCancelled(true);
				return;
			}
		}

		/*
		 * cast on-hit abilities and add the extra damage to the damage event
		 */
		result.applyOnHitEffects(stats == null ? stats = playerData.getStats().newTemporary() : stats, target);

		event.setDamage(result.getDamage());
	}

	@EventHandler
	public void c(BlockBreakEvent event) {
		Player player = event.getPlayer();
		Block block = event.getBlock();
		if (player.getGameMode() == GameMode.CREATIVE)
			return;

		NBTItem item = MMOItems.plugin.getNMS().getNBTItem(player.getInventory().getItemInMainHand());
		if (!item.hasType())
			return;

		Tool tool = new Tool(player, item, item.getType());
		if (!tool.canBeUsed()) {
			event.setCancelled(true);
			return;
		}

		if (tool.miningEffects(block))
			event.setCancelled(true);
	}

	@EventHandler
	public void d(PlayerInteractEntityEvent event) {
		Player player = event.getPlayer();
		if (!(event.getRightClicked() instanceof LivingEntity))
			return;

		NBTItem item = MMOItems.plugin.getNMS().getNBTItem(player.getInventory().getItemInMainHand());
		if (!item.hasType())
			return;

		LivingEntity target = (LivingEntity) event.getRightClicked();
		if (!MMOUtils.canDamage(player, target))
			return;

		UseItem weapon = UseItem.getItem(player, item, item.getType());
		if (!weapon.canBeUsed())
			return;

		// special staff attack
		if (weapon instanceof Staff)
			((Staff) weapon).specialAttack(target);

		// special gauntlet attack
		if (weapon instanceof Gauntlet)
			((Gauntlet) weapon).specialAttack(target);
	}

	@EventHandler
	public void e(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		if (event.getAction() != InventoryAction.SWAP_WITH_CURSOR || event.getInventory().getHolder() instanceof AdvancedWorkbench)
			return;

		NBTItem item = MMOItems.plugin.getNMS().getNBTItem(event.getCursor());
		if (!item.hasType())
			return;

		UseItem useItem = UseItem.getItem(player, item, item.getType());
		if (!useItem.canBeUsed())
			return;

		if (useItem instanceof GemStone) {
			NBTItem picked = MMOItems.plugin.getNMS().getNBTItem(event.getCurrentItem());
			if (!picked.hasType())
				return;

			ApplyResult result = ((GemStone) useItem).applyOntoItem(picked, picked.getType());
			if (result.getType() == ResultType.NONE)
				return;

			event.setCancelled(true);
			item.getItem().setAmount(item.getItem().getAmount() - 1);

			if (result.getType() == ResultType.FAILURE)
				return;

			event.setCurrentItem(result.getResult());
		}

		if (useItem instanceof Consumable && event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
			event.setCancelled(true);
			if (((Consumable) useItem).useOnItem(event, MMOItems.plugin.getNMS().getNBTItem(event.getCurrentItem())))
				event.getCursor().setAmount(event.getCursor().getAmount() - 1);
		}
	}

	@EventHandler
	public void f(EntityShootBowEvent event) {
		if (!(event.getProjectile() instanceof Arrow) || !(event.getEntity() instanceof Player))
			return;

		NBTItem item = MMOItems.plugin.getNMS().getNBTItem(event.getBow());
		Type type = item.getType();

		PlayerData playerData = PlayerData.get((Player) event.getEntity());
		if (type != null)
			if (!new Weapon(playerData, item, type).canBeUsed()) {
				event.setCancelled(true);
				return;
			}

		MMOItems.plugin.getEntities().registerCustomProjectile(item, playerData.getStats().newTemporary(), (Arrow) event.getProjectile(), type != null, event.getForce());
	}

	@EventHandler
	public void g(PlayerItemConsumeEvent event) {
		NBTItem item = MMOItems.plugin.getNMS().getNBTItem(event.getItem());
		if (!item.hasType())
			return;

		Player player = event.getPlayer();
		UseItem useItem = UseItem.getItem(player, item, item.getType());
		if (!useItem.canBeUsed()) {
			event.setCancelled(true);
			return;
		}

		if (useItem instanceof Consumable) {

			if (!useItem.getPlayerData().canUseItem(useItem.getMMOItem().getId())) {
				Message.ITEM_ON_COOLDOWN.format(ChatColor.RED).send(player);
				return;
			}

			if (!((Consumable) useItem).useWithoutItem(true)) {
				event.setCancelled(true);
				return;
			}

			useItem.getPlayerData().applyItemCooldown(useItem.getMMOItem().getId(), useItem.getNBTItem().getStat(ItemStat.ITEM_COOLDOWN));
			useItem.executeCommands();
		}
	}
}
