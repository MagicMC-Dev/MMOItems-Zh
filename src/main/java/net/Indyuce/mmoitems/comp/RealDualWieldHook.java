package net.Indyuce.mmoitems.comp;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;

import com.evill4mer.RealDualWield.Api.PlayerDamageEntityWithOffhandEvent;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.TypeSet;
import net.Indyuce.mmoitems.api.interaction.weapon.Weapon;
import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.Indyuce.mmoitems.api.player.damage.AttackResult;
import net.Indyuce.mmoitems.api.player.damage.AttackResult.DamageType;

public class RealDualWieldHook implements Listener {
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void a(PlayerDamageEntityWithOffhandEvent event) {

		/*
		 * Citizens and Sentinels NPC support; damage = 0 check to ignore safety
		 * checks; check for entity attack
		 */
		if (event.getDamage() == 0 || !(event.getEntity() instanceof LivingEntity) || event.getEntity().hasMetadata("NPC"))
			return;

		// custom damage check
		LivingEntity target = (LivingEntity) event.getEntity();
		if (MMOItems.plugin.getDamage().findInfo(target) != null)
			return;

		Player player = event.getPlayer();
		TemporaryStats stats = null;

		/*
		 * must apply attack conditions before apply any effects. the event must
		 * be cancelled before anything is applied
		 */
		PlayerData playerData = PlayerData.get(player);
		NBTItem item = MMOItems.plugin.getNMS().getNBTItem(player.getInventory().getItemInMainHand());
		NBTItem offhandItem = MMOItems.plugin.getNMS().getNBTItem(player.getInventory().getItemInOffHand());
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
		if (offhandItem.hasType()) {
			Weapon weapon = new Weapon(playerData, offhandItem, offhandItem.getType());

			if (weapon.getMMOItem().getType().getItemSet() == TypeSet.RANGE) {
				event.setCancelled(true);
				return;
			}

			if (!weapon.canBeUsed()) {
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
}
