package net.Indyuce.mmoitems.listener;

import java.text.DecimalFormat;

import net.Indyuce.mmoitems.api.player.inventory.EquippedItem;
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

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.TypeSet;
import net.Indyuce.mmoitems.api.interaction.Consumable;
import net.Indyuce.mmoitems.api.interaction.GemStone;
import net.Indyuce.mmoitems.api.interaction.ItemSkin;
import net.Indyuce.mmoitems.api.interaction.Tool;
import net.Indyuce.mmoitems.api.interaction.UseItem;
import net.Indyuce.mmoitems.api.interaction.weapon.Gauntlet;
import net.Indyuce.mmoitems.api.interaction.weapon.Weapon;
import net.Indyuce.mmoitems.api.interaction.weapon.untargeted.Staff;
import net.Indyuce.mmoitems.api.interaction.weapon.untargeted.UntargetedWeapon;
import net.Indyuce.mmoitems.api.interaction.weapon.untargeted.UntargetedWeapon.WeaponType;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.DamageType;
import net.mmogroup.mmolib.api.item.NBTItem;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemUse implements Listener {
	private static final DecimalFormat digit = new DecimalFormat("0.#");

	@EventHandler
	public void a(PlayerInteractEvent event) {
		if (!event.hasItem())
			// || event.getHand() != EquipmentSlot.HAND
			return;

		NBTItem item = MMOLib.plugin.getVersion().getWrapper().getNBTItem(event.getItem());
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

		/*
		 * (BUG FIX) cancel the event to prevent things like shield blocking
		 */
		if (!useItem.applyItemCosts()) {
			event.setCancelled(true);
			return;
		}

		// commands & consummables
		if (event.getAction().name().contains("RIGHT_CLICK")) {
			if (!useItem.getPlayerData().canUseItem(useItem.getMMOItem().getId())) {
				Message.ITEM_ON_COOLDOWN
						.format(ChatColor.RED, "#left#", digit.format(useItem.getPlayerData().getItemCooldown(useItem.getMMOItem().getId())))
						.send(player, "item-cooldown");
				event.setCancelled(true);
				return;
			}

			useItem.getPlayerData().applyItemCooldown(useItem.getMMOItem().getId(), useItem.getNBTItem().getStat("ITEM_COOLDOWN"));
			useItem.executeCommands();

			if (useItem instanceof Consumable) {
				event.setCancelled(true);
				if (((Consumable) useItem).useWithoutItem())
					event.getItem().setAmount(event.getItem().getAmount() - 1);
			}
		}

		if (useItem instanceof UntargetedWeapon) {
			UntargetedWeapon weapon = (UntargetedWeapon) useItem;
			if ((event.getAction().name().contains("RIGHT_CLICK") && weapon.getWeaponType() == WeaponType.RIGHT_CLICK)
					|| (event.getAction().name().contains("LEFT_CLICK") && weapon.getWeaponType() == WeaponType.LEFT_CLICK))
				weapon.untargetedAttack(event.getHand());
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void b(EntityDamageByEntityEvent event) {

		/*
		 * Citizens and Sentinels NPC support; damage = 0 check to ignore safety
		 * checks; check for entity attack
		 */
		if (event.getDamage() == 0 || event.getCause() != DamageCause.ENTITY_ATTACK || !(event.getEntity() instanceof LivingEntity)
				|| !(event.getDamager() instanceof Player) || event.getEntity().hasMetadata("NPC") || event.getDamager().hasMetadata("NPC"))
			return;

		// custom damage check
		LivingEntity target = (LivingEntity) event.getEntity();
		if (MMOLib.plugin.getDamage().findInfo(target) != null)
			return;

		Player player = (Player) event.getDamager();
		CachedStats stats = null;

		/*
		 * must apply attack conditions before apply any effects. the event must
		 * be cancelled before anything is applied
		 */
		PlayerData playerData = PlayerData.get(player);
		NBTItem item = MMOLib.plugin.getVersion().getWrapper().getNBTItem(player.getInventory().getItemInMainHand());
		ItemAttackResult result = new ItemAttackResult(event.getDamage(), DamageType.WEAPON, DamageType.PHYSICAL);

		if (item.hasType() && Type.get(item.getType()) != Type.BLOCK) {
			Weapon weapon = new Weapon(playerData, item);

			if (weapon.getMMOItem().getType().getItemSet() == TypeSet.RANGE) {
				event.setCancelled(true);
				return;
			}

			if (!weapon.applyItemCosts()) {
				event.setCancelled(true);
				return;
			}

			weapon.handleTargetedAttack(stats = playerData.getStats().newTemporary(), target, result);
			if (!result.isSuccessful()) {
				event.setCancelled(true);
				return;
			}
		}

		/*
		 * cast on-hit abilities and add the extra damage to the damage event
		 */
		result.applyEffects(stats == null ? playerData.getStats().newTemporary() : stats, item, target);
		event.setDamage(result.getDamage());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void c(BlockBreakEvent event) {
		Player player = event.getPlayer();
		Block block = event.getBlock();
		if (player.getGameMode() == GameMode.CREATIVE)
			return;

		NBTItem item = MMOLib.plugin.getVersion().getWrapper().getNBTItem(player.getInventory().getItemInMainHand());
		if (!item.hasType())
			return;

		Tool tool = new Tool(player, item);
		if (!tool.applyItemCosts()) {
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

		NBTItem item = MMOLib.plugin.getVersion().getWrapper().getNBTItem(player.getInventory().getItemInMainHand());
		if (!item.hasType())
			return;

		LivingEntity target = (LivingEntity) event.getRightClicked();
		if (!MMOUtils.canDamage(player, target))
			return;

		UseItem weapon = UseItem.getItem(player, item, item.getType());
		if (!weapon.applyItemCosts())
			return;

		// special staff attack
		if (weapon instanceof Staff)
			((Staff) weapon).specialAttack(target);

		// special gauntlet attack
		if (weapon instanceof Gauntlet)
			((Gauntlet) weapon).specialAttack(target);
	}

	// TODO: Rewrite this with a custom 'ApplyMMOItemEvent'?
	@EventHandler
	public void e(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		if (event.getAction() != InventoryAction.SWAP_WITH_CURSOR)
			return;

		NBTItem item = MMOLib.plugin.getVersion().getWrapper().getNBTItem(event.getCursor());
		if (!item.hasType())
			return;

		UseItem useItem = UseItem.getItem(player, item, item.getType());
		if (!useItem.applyItemCosts())
			return;

		if (useItem instanceof ItemSkin) {
			NBTItem picked = MMOLib.plugin.getVersion().getWrapper().getNBTItem(event.getCurrentItem());
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
			NBTItem picked = MMOLib.plugin.getVersion().getWrapper().getNBTItem(event.getCurrentItem());
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
			if (((Consumable) useItem).useOnItem(event, MMOLib.plugin.getVersion().getWrapper().getNBTItem(event.getCurrentItem()))) {
				event.setCancelled(true);
				event.getCursor().setAmount(event.getCursor().getAmount() - 1);
			}
	}

	@EventHandler
	public void f(EntityShootBowEvent event) {
		if (!(event.getProjectile() instanceof Arrow) || !(event.getEntity() instanceof Player))
			return;

		NBTItem item = MMOLib.plugin.getVersion().getWrapper().getNBTItem(event.getBow());
		Type type = Type.get(item.getType());

		PlayerData playerData = PlayerData.get((Player) event.getEntity());
		if (type != null) {
			Weapon weapon = new Weapon(playerData, item);
			if (!weapon.applyItemCosts() || !weapon.applyWeaponCosts()) {
				event.setCancelled(true);
				return;
			}
			if (!eitherHandSuccess((Player) event.getEntity(), item, event.getHand())) {
				event.setCancelled(true);
				return;
			}
		}

		Arrow arrow = (Arrow) event.getProjectile();
		if (item.getStat("ARROW_VELOCITY") > 0)
			arrow.setVelocity(arrow.getVelocity().multiply(item.getStat("ARROW_VELOCITY")));
		MMOItems.plugin.getEntities().registerCustomProjectile(item, playerData.getStats().newTemporary(), event.getProjectile(), type != null,
				event.getForce());
	}

	/**
	 * It is undesirable to fire bows whose arrows have the stats of the Mainhand weapon.
	 * <p></p>
	 * If such an item succeeds in this check, that means it can be fired. Only makes sense to
	 * check when you really suspect it is an EITHER_HAND weapon. Otherwise the check already
	 * happened when its stats where added in the first place.
	 */
	public static boolean eitherHandSuccess(@NotNull Player p, @NotNull NBTItem item, @NotNull EquipmentSlot held) {

		/*
		 * Make sure it is a plugin item to begin with
		 */
		Type itemType = Type.get(item.getType());
		if (itemType == null) { return false; }

		/*
		 * Get the held type of the item held
		 */
		Type.EquipmentSlot mainheld_type = null;

		// Get Mainhand Item
		ItemStack mainheld = p.getInventory().getItemInMainHand();

		// Existed?
		if (mainheld.getType().isItem()) {

			// Get as NBTItem
			NBTItem mainnbt = MMOLib.plugin.getVersion().getWrapper().getNBTItem(mainheld);

			// Existed?
			if (mainnbt != null) {

				// Get MMOItem Type
				Type maintype = Type.get(mainnbt.getType());

				// Existed?
				if (maintype != null) {

					// Remember Equipment Type
					mainheld_type = maintype.getEquipmentType();
				}
			}
		}

		/*
		 * Perform Equipped Item Check
		 */
		Type.EquipmentSlot heldTranslated;
		switch (held) {
			case HAND:
				heldTranslated = Type.EquipmentSlot.MAIN_HAND;
				break;
			case OFF_HAND:
				heldTranslated = Type.EquipmentSlot.OFF_HAND;
				break;
			case FEET:
			case HEAD:
			case LEGS:
			case CHEST:
				heldTranslated = Type.EquipmentSlot.ARMOR;
				break;
			default:
				heldTranslated = Type.EquipmentSlot.ACCESSORY;
				break;
		}

		// Just do as if it was actually a real thing
		EquippedItem eitem = new EquippedItem(item, heldTranslated);

		// Does it match?
		return eitem.matches(itemType, mainheld_type);
	}

	@EventHandler
	public void g(PlayerItemConsumeEvent event) {
		NBTItem item = MMOLib.plugin.getVersion().getWrapper().getNBTItem(event.getItem());
		if (!item.hasType())
			return;

		Player player = event.getPlayer();
		UseItem useItem = UseItem.getItem(player, item, item.getType());
		if (!useItem.applyItemCosts()) {
			event.setCancelled(true);
			return;
		}

		if (useItem instanceof Consumable) {

			if (!useItem.getPlayerData().canUseItem(useItem.getMMOItem().getId())) {
				Message.ITEM_ON_COOLDOWN.format(ChatColor.RED).send(player);
				return;
			}

			if (!((Consumable) useItem).useWithoutItem()) {
				event.setCancelled(true);
				return;
			}

			useItem.getPlayerData().applyItemCooldown(useItem.getMMOItem().getId(), useItem.getNBTItem().getStat("ITEM_COOLDOWN"));
			useItem.executeCommands();
		}
	}
}
