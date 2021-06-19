package net.Indyuce.mmoitems.comp;

import com.evill4mer.RealDualWield.Api.PlayerDamageEntityWithOffhandEvent;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.DamageType;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.TypeSet;
import net.Indyuce.mmoitems.api.interaction.weapon.Weapon;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

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
		if (MythicLib.plugin.getDamage().findInfo(target) != null)
			return;

		/*
		 * must apply attack conditions before apply any effects. the event must
		 * be cancelled before anything is applied
		 */
		Player player = event.getPlayer();
		PlayerData playerData = PlayerData.get(player);
		NBTItem offhandItem = MythicLib.plugin.getVersion().getWrapper().getNBTItem(player.getInventory().getItemInOffHand());
		ItemAttackResult result = new ItemAttackResult(event.getDamage(), DamageType.WEAPON, DamageType.PHYSICAL);

		if (offhandItem.hasType()) {
			Weapon weapon = new Weapon(playerData, offhandItem);

			if (weapon.getMMOItem().getType().getItemSet() == TypeSet.RANGE) {
				event.setCancelled(true);
				return;
			}

			if (!weapon.checkItemRequirements()) {
				event.setCancelled(true);
				return;
			}
		}

		/*
		 * cast on-hit abilities and add the extra damage to the damage event
		 */
		result.applyEffects(playerData.getStats().newTemporary(EquipmentSlot.OFF_HAND), offhandItem, target);
		event.setDamage(result.getDamage());
	}
}
