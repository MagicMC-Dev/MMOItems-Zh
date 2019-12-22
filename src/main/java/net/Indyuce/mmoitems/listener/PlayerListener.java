package net.Indyuce.mmoitems.listener;

import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.ability.Magical_Shield;
import net.Indyuce.mmoitems.api.Ability.CastingMode;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.SoulboundInfo;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.PlayerStats;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.DamageType;

public class PlayerListener implements Listener {
	@EventHandler(priority = EventPriority.HIGH)
	public void b(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player) || event.isCancelled() || event.getEntity().hasMetadata("NPC"))
			return;

		Player player = (Player) event.getEntity();

		// magical shield ability
		for (Location loc : Magical_Shield.magicalShield.keySet())
			if (loc.getWorld().equals(player.getWorld())) {
				Double[] values = Magical_Shield.magicalShield.get(loc);
				if (loc.distanceSquared(player.getLocation()) <= values[0])
					event.setDamage(event.getDamage() * (1 - Math.max(values[1], 1)));
			}

		/*
		 * damage reduction
		 */
		if (MMOLib.plugin.getDamage().findInfo(player) != null)
			return;
		PlayerStats stats = PlayerData.get(player).getStats();

		if (event.getCause() == DamageCause.FIRE)
			event.setDamage(event.getDamage() * (1 - stats.getStat(ItemStat.FIRE_DAMAGE_REDUCTION) / 100));
		else if (event.getCause() == DamageCause.FALL)
			event.setDamage(event.getDamage() * (1 - stats.getStat(ItemStat.FALL_DAMAGE_REDUCTION) / 100));
		else if (event.getCause() == DamageCause.MAGIC)
			event.setDamage(event.getDamage() * (1 - stats.getStat(ItemStat.MAGIC_DAMAGE_REDUCTION) / 100));
		else if (event.getCause() == DamageCause.PROJECTILE)
			event.setDamage(event.getDamage() * (1 - stats.getStat(ItemStat.PROJECTILE_DAMAGE_REDUCTION) / 100));
		else if (event.getCause() == DamageCause.ENTITY_ATTACK || event.getCause() == DamageCause.ENTITY_SWEEP_ATTACK || event.getCause() == DamageCause.PROJECTILE)
			event.setDamage(event.getDamage() * (1 - stats.getStat(ItemStat.PHYSICAL_DAMAGE_REDUCTION) / 100));

		event.setDamage(event.getDamage() * (1 - stats.getStat(ItemStat.DAMAGE_REDUCTION) / 100));
	}

	// regeneration
	@EventHandler
	public void c(EntityRegainHealthEvent event) {
		if (event.getEntity() instanceof Player)
			event.setAmount(event.getAmount() * (1 + PlayerData.get((Player) event.getEntity()).getStats().getStat(ItemStat.REGENERATION) / 100));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void d(PlayerJoinEvent event) {
		PlayerData.load(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void e(PlayerQuitEvent event) {
		PlayerData.get(event.getPlayer()).save();
	}

	// apply on-hit abilities from armor pieces.
	@EventHandler(priority = EventPriority.HIGH)
	public void f(EntityDamageByEntityEvent event) {
		if (event.isCancelled() || !(event.getEntity() instanceof Player) || !(event.getDamager() instanceof LivingEntity) || event.getEntity().hasMetadata("NPC") || event.getDamager().hasMetadata("NPC"))
			return;

		LivingEntity damager = (LivingEntity) event.getDamager();
		Player player = (Player) event.getEntity();
		PlayerData.get(player).castAbilities(damager, new ItemAttackResult(event.getDamage(), DamageType.SKILL), CastingMode.WHEN_HIT);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void g(PlayerInteractEvent event) {
		if (event.getAction() == Action.PHYSICAL)
			return;

		Player player = event.getPlayer();
		boolean left = event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK;
		PlayerData.get(player).castAbilities(null, new ItemAttackResult(true, DamageType.SKILL), player.isSneaking() ? (left ? CastingMode.SHIFT_LEFT_CLICK : CastingMode.SHIFT_RIGHT_CLICK) : (left ? CastingMode.LEFT_CLICK : CastingMode.RIGHT_CLICK));
	}

	/*
	 * prevent players from droping items which are bound to them with a
	 * soulbound. items are cached inside a map waiting for the player to
	 * respawn. if he does not respawn the items are dropped on the ground, this
	 * way there don't get lost
	 */
	@EventHandler
	public void h(PlayerDeathEvent event) {
		if (event.getKeepInventory())
			return;

		Player player = event.getEntity();
		SoulboundInfo soulboundInfo = new SoulboundInfo(player);

		ItemStack item;
		Iterator<ItemStack> iterator = event.getDrops().iterator();
		while (iterator.hasNext())
			if (MMOLib.plugin.getNMS().getNBTItem(item = iterator.next()).getString("MMOITEMS_SOULBOUND").equals(player.getUniqueId().toString())) {
				iterator.remove();
				soulboundInfo.add(item);
			}

		soulboundInfo.setup();
	}

	@EventHandler
	public void i(PlayerRespawnEvent event) {
		SoulboundInfo.read(event.getPlayer());
	}
}