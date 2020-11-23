package net.Indyuce.mmoitems.listener;

import java.util.Iterator;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.SoulboundInfo;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.ability.Ability.CastingMode;
import net.Indyuce.mmoitems.api.interaction.util.InteractItem;
import net.Indyuce.mmoitems.api.interaction.weapon.Weapon;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.DamageType;
import net.mmogroup.mmolib.api.item.NBTItem;

public class PlayerListener implements Listener {

	@EventHandler
	public void loadPlayerData(PlayerJoinEvent event) {
		MMOItems.plugin.getRecipes().refreshRecipeBook(event.getPlayer());
		PlayerData.load(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void savePlayerData(PlayerQuitEvent event) {
		PlayerData.get(event.getPlayer()).save();
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void castWhenHitAbilities(EntityDamageByEntityEvent event) {
		if (!(event.getEntity() instanceof Player) || event.getEntity().hasMetadata("NPC"))
			return;

		LivingEntity damager = MMOUtils.getDamager(event);
		if (damager == null)
			return;

		Player player = (Player) event.getEntity();
		PlayerData.get(player).castAbilities(damager, new ItemAttackResult(event.getDamage(), DamageType.SKILL), CastingMode.WHEN_HIT);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void castClickAbilities(PlayerInteractEvent event) {
		if (event.getAction() == Action.PHYSICAL)
			return;

		Player player = event.getPlayer();
		boolean left = event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK;
		PlayerData.get(player).castAbilities(null, new ItemAttackResult(true, DamageType.SKILL),
				player.isSneaking() ? (left ? CastingMode.SHIFT_LEFT_CLICK : CastingMode.SHIFT_RIGHT_CLICK)
						: (left ? CastingMode.LEFT_CLICK : CastingMode.RIGHT_CLICK));
	}

	/*
	 * prevent players from droping items which are bound to them with a
	 * soulbound. items are cached inside a map waiting for the player to
	 * respawn. if he does not respawn the items are dropped on the ground, this
	 * way there don't get lost
	 */
	@EventHandler(priority = EventPriority.HIGH)
	public void applySoulbound(PlayerDeathEvent event) {
		if (event.getKeepInventory())
			return;

		Player player = event.getEntity();
		SoulboundInfo soulboundInfo = new SoulboundInfo(player);

		Iterator<ItemStack> iterator = event.getDrops().iterator();
		while (iterator.hasNext()) {
			ItemStack item = iterator.next();
			NBTItem nbt = MMOLib.plugin.getVersion().getWrapper().getNBTItem(item);

			/*
			 * not a perfect check but it's very sufficient and so we avoid
			 * using a JsonParser followed by map checkups in the SoulboundData
			 * constructor
			 */
			if (nbt.hasTag("MMOITEMS_SOULBOUND") && nbt.getString("MMOITEMS_SOULBOUND").contains(player.getUniqueId().toString())) {
				iterator.remove();
				soulboundInfo.add(item);
			}
		}

		if (soulboundInfo.hasItems())
			soulboundInfo.setup();
	}

	@EventHandler
	public void readSoulbound(PlayerRespawnEvent event) {
		SoulboundInfo.read(event.getPlayer());
	}

	@EventHandler(ignoreCancelled = true)
	public void registerProjectiles(ProjectileLaunchEvent event) {
		if (!(event.getEntity() instanceof Trident) || !(event.getEntity().getShooter() instanceof Player))
			return;

		InteractItem item = new InteractItem((Player) event.getEntity().getShooter(), Material.TRIDENT);
		if (!item.hasItem())
			return;

		NBTItem nbtItem = MMOLib.plugin.getVersion().getWrapper().getNBTItem(item.getItem());
		Type type = nbtItem.getType();

		PlayerData playerData = PlayerData.get((Player) event.getEntity().getShooter());
		if (type != null && !new Weapon(playerData, nbtItem).canBeUsed()) {
			event.setCancelled(true);
			return;
		}

		MMOItems.plugin.getEntities().registerCustomProjectile(nbtItem, playerData.getStats().newTemporary(), event.getEntity(),
				type != null);
	}
}