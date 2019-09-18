package net.Indyuce.mmoitems.listener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.interaction.util.DurabilityItem;
import net.Indyuce.mmoitems.api.interaction.util.InteractItem;

public class CustomDurability implements Listener {
	private final List<DamageCause> applyDamageCauses = Arrays.asList(
		DamageCause.ENTITY_ATTACK, DamageCause.ENTITY_EXPLOSION, DamageCause.BLOCK_EXPLOSION, DamageCause.THORNS,
		DamageCause.CONTACT, DamageCause.FIRE, DamageCause.HOT_FLOOR, DamageCause.LAVA, DamageCause.PROJECTILE );
	private final List<String> hoeableBlocks = Arrays.asList("GRASS_PATH", "GRASS", "DIRT");
	private final List<PlayerFishEvent.State> applyFishStates = Arrays.asList(State.IN_GROUND, State.CAUGHT_ENTITY, State.CAUGHT_FISH);

	/*
	 * if the player switches his glide more than once a second, the old
	 * runnable needs to be cancelled so it doesn't consume twice as much
	 * durability
	 */
	private Map<UUID, BukkitRunnable> elytraDurabilityLoss = new HashMap<>();

	/*
	 * when breaking a block, ANY item will lose one durability point even if
	 * the block can be broken instantly.
	 */
	@EventHandler(priority = EventPriority.HIGH)
	public void a(BlockBreakEvent event) {
		if (event.isCancelled())
			return;

		Player player = event.getPlayer();
		ItemStack item = player.getInventory().getItemInMainHand();
		DurabilityItem durItem = new DurabilityItem(player, item);
		if (durItem.isValid()) {
			ItemStack result = durItem.decreaseDurability(1).toItem();
			if (result != null && result.getType() != Material.AIR)
				item.setItemMeta(result.getItemMeta());
		}
	}

	/*
	 * when getting hit, any armor piece will lose 1 durability point
	 * (explosions deal 2 points of damage)
	 */
	@EventHandler(priority = EventPriority.HIGH)
	public void c(EntityDamageEvent event) {
		if (event.isCancelled() || !(event.getEntity() instanceof Player))
			return;

		if (!applyDamageCauses.contains(event.getCause()))
			return;
		
		int dura = 1;
		if(event.getCause() == DamageCause.BLOCK_EXPLOSION ||
		   event.getCause() == DamageCause.ENTITY_EXPLOSION)
			dura = 2;

		Player player = (Player) event.getEntity();
		DurabilityItem durabilityItem;

		ItemStack helmet = player.getInventory().getHelmet();
		if (helmet != null)
			if ((durabilityItem = new DurabilityItem(player, helmet)).isValid())
				player.getInventory().setHelmet(durabilityItem.decreaseDurability(dura).toItem());

		ItemStack chestplate = player.getInventory().getChestplate();
		if (chestplate != null)
			if ((durabilityItem = new DurabilityItem(player, chestplate)).isValid())
				player.getInventory().setChestplate(durabilityItem.decreaseDurability(dura).toItem());

		ItemStack leggings = player.getInventory().getLeggings();
		if (leggings != null)
			if ((durabilityItem = new DurabilityItem(player, leggings)).isValid())
				player.getInventory().setLeggings(durabilityItem.decreaseDurability(dura).toItem());

		ItemStack boots = player.getInventory().getBoots();
		if (boots != null)
			if ((durabilityItem = new DurabilityItem(player, boots)).isValid())
				player.getInventory().setBoots(durabilityItem.decreaseDurability(dura).toItem());

		InteractItem intItem = new InteractItem(player, Material.SHIELD);
		if (intItem.hasItem() && player.isBlocking())
			if ((durabilityItem = new DurabilityItem(player, intItem.getItem())).isValid())
				intItem.setItem(durabilityItem.decreaseDurability(dura).toItem());
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void d(EntityShootBowEvent event) {
		if (event.isCancelled() || !(event.getEntity() instanceof Player))
			return;

		Player player = (Player) event.getEntity();
		DurabilityItem durItem = new DurabilityItem(player, event.getBow());
		if (durItem.isValid())
			event.getBow().setItemMeta(durItem.decreaseDurability(1).toItem().getItemMeta());
	}

	/*
	 * make shears lose durability when used on a sheep
	 */
	@EventHandler(priority = EventPriority.HIGH)
	public void e(PlayerShearEntityEvent event) {
		if (event.isCancelled())
			return;

		Player player = event.getPlayer();
		InteractItem intItem = new InteractItem(player, Material.SHEARS);
		if (!intItem.hasItem())
			return;

		DurabilityItem durItem = new DurabilityItem(player, intItem.getItem());
		if (durItem.isValid())
			intItem.setItem(durItem.decreaseDurability(1).toItem());
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGH)
	public void f(PlayerInteractEvent event) {
		if (event.isCancelled() || !event.hasItem() || event.getAction() != Action.RIGHT_CLICK_BLOCK || !event.hasBlock())
			return;

		Player player = event.getPlayer();
		InteractItem intItem;
		Material material = event.getClickedBlock().getType();

		/*
		 * making hoe lose durability when hoeing grass/dirt/coarse
		 */
		if (hoeableBlocks.contains(material.name()) && (intItem = new InteractItem(player, "_HOE")).hasItem()) {
			DurabilityItem durItem = new DurabilityItem(player, intItem.getItem());
			if (durItem.isValid())
				intItem.setItem(durItem.decreaseDurability(1).toItem());
		}

		/*
		 * grass path creation
		 */
		else if (material == Material.GRASS && (intItem = new InteractItem(player, "_SPADE")).hasItem()) {
			DurabilityItem durItem = new DurabilityItem(player, intItem.getItem());
			if (durItem.isValid())
				intItem.setItem(durItem.decreaseDurability(1).toItem());
		}

		/*
		 * stripped logs
		 */
		else if (material.name().endsWith("_LOG") && !material.name().startsWith("STRIPPED_") && (intItem = new InteractItem(player, "_AXE")).hasItem()) {
			DurabilityItem durItem = new DurabilityItem(player, intItem.getItem());
			if (durItem.isValid())
				intItem.setItem(durItem.decreaseDurability(1).toItem());
		}
	}

	/*
	 * flint and steel consuming
	 */
	@EventHandler(priority = EventPriority.HIGH)
	public void g(BlockIgniteEvent event) {
		if (event.isCancelled() || !(event.getIgnitingEntity() instanceof Player))
			return;

		Player player = (Player) event.getIgnitingEntity();
		InteractItem intItem = new InteractItem(player, Material.FLINT_AND_STEEL);
		if (!intItem.hasItem())
			return;

		DurabilityItem durItem = new DurabilityItem(player, intItem.getItem());
		if (durItem.isValid())
			intItem.setItem(durItem.decreaseDurability(1).toItem());
	}

	/*
	 * fishing rod durability loss - loses 1 if it catches a fish successfully.
	 * if it catches an item, uses 3 durability, 5 if it is any other entity,
	 * and 0 durability loss if it does not catch anything ; a delay is needed
	 * otherwise it does not update the item held by the player
	 */
	@EventHandler(priority = EventPriority.HIGH)
	public void h(PlayerFishEvent event) {
		if (event.isCancelled() || !applyFishStates.contains(event.getState()))
			return;

		Player player = event.getPlayer();
		InteractItem intItem = new InteractItem(player, Material.FISHING_ROD);
		if (!intItem.hasItem())
			return;

		DurabilityItem durItem = new DurabilityItem(player, intItem.getItem());
		if (!durItem.isValid())
			return;

		new BukkitRunnable() {
			public void run() {
				int loss = event.getState() == State.CAUGHT_FISH ? 1 : (event.getState() == State.CAUGHT_ENTITY ? (event.getCaught() instanceof Item ? 3 : 5) : 2);
				intItem.setItem(durItem.decreaseDurability(loss).toItem());
			}
		}.runTaskLater(MMOItems.plugin, 0);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void i(EntityToggleGlideEvent event) {
		if (event.isCancelled() || !(event.getEntity() instanceof Player) || !(event.isGliding()))
			return;

		Player player = (Player) event.getEntity();
		if (elytraDurabilityLoss.containsKey(player.getUniqueId()))
			elytraDurabilityLoss.get(player.getUniqueId()).cancel();

		BukkitRunnable runnable = new BukkitRunnable() {
			public void run() {
				if (player == null || !player.isOnline() || !player.isGliding()) {
					cancel();
					elytraDurabilityLoss.remove(player.getUniqueId());
					return;
				}

				ItemStack elytra = player.getInventory().getChestplate();
				if (elytra != null && elytra.getType() == Material.ELYTRA) {
					DurabilityItem durabilityItem = new DurabilityItem(player, elytra);
					if (durabilityItem.isValid())
						player.getInventory().setChestplate(durabilityItem.decreaseDurability(1).toItem());
				}
			}
		};

		elytraDurabilityLoss.put(player.getUniqueId(), runnable);
		runnable.runTaskTimer(MMOItems.plugin, 10, 20);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void j(PlayerItemMendEvent event) {
		if (event.isCancelled())
			return;

		DurabilityItem durItem = new DurabilityItem(event.getPlayer(), event.getItem());
		if (durItem.isValid())
			event.getItem().setItemMeta(durItem.addDurability(event.getRepairAmount()).toItem().getItemMeta());
	}
}
