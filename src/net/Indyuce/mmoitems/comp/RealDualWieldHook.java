package net.Indyuce.mmoitems.comp;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;

import com.evill4mer.RealDualWield.Api.PlayerDamageEntityWithOffhandEvent;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Ability.CastingMode;
import net.Indyuce.mmoitems.api.AttackResult;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.TypeSet;
import net.Indyuce.mmoitems.api.interaction.weapon.Weapon;
import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;

public class RealDualWieldHook implements Listener {
	@EventHandler(priority = EventPriority.HIGH)
	public void a(PlayerDamageEntityWithOffhandEvent event) {

		// check for npc
		// safety checks
		if (event.getEntity().hasMetadata("NPC") || event.isCancelled() || !(event.getEntity() instanceof LivingEntity))
			return;

		// custom damage check
		LivingEntity target = (LivingEntity) event.getEntity();
		if (MMOItems.plugin.getDamage().isDamaged(target) || !MMOItems.plugin.getRPG().canBeDamaged(target))
			return;

		/*
		 * cast on-hit abilities and add the extra damage to the damage event
		 */
		Player player = event.getPlayer();
		PlayerData playerData = PlayerData.get(player);
		TemporaryStats stats = playerData.getStats().newTemporary();
		AttackResult result = playerData.castAbilities(stats, target, new AttackResult(true, event.getDamage()), CastingMode.ON_HIT);
		event.setDamage(result.getDamage());

		NBTItem item = MMOItems.plugin.getNMS().getNBTItem(player.getInventory().getItemInOffHand());
		Type type = item.getType();
		if (type == null)
			return;

		Weapon weapon = new Weapon(playerData, item, type);

		// can't attack melee
		if (type.getItemSet() == TypeSet.RANGE) {
			event.setCancelled(true);
			return;
		}

		if (!weapon.canBeUsed()) {
			event.setCancelled(true);
			return;
		}

		weapon.targetedAttack(stats, target, EquipmentSlot.OFF_HAND, result);
		if (!result.isSuccessful()) {
			event.setCancelled(true);
			return;
		}

		event.setDamage(result.getDamage());
	}
}
