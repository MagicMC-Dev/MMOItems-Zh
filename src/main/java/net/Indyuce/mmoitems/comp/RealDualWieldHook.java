package net.Indyuce.mmoitems.comp;

import com.evill4mer.RealDualWield.Api.PlayerDamageEntityWithOffhandEvent;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
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

		// Citizens NPC support; also check if it's not a useless event
		if (event.getDamage() == 0 || !(event.getEntity() instanceof LivingEntity) || event.getEntity().hasMetadata("NPC"))
			return;

		// Custom damage check
		LivingEntity target = (LivingEntity) event.getEntity();
		if (MythicLib.plugin.getDamage().findInfo(target) != null)
			return;

		/*
		 * Must apply attack conditions before apply any effects.
		 * The event must be cancelled before anything is applied
		 */
		Player player = event.getPlayer();
		PlayerData playerData = PlayerData.get(player);
		NBTItem offhandItem = MythicLib.plugin.getVersion().getWrapper().getNBTItem(player.getInventory().getItemInOffHand());

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

		// Cast on-hit abilities and add extra damage to the Bukkit event
		ItemAttackMetadata attack = new ItemAttackMetadata(new DamageMetadata(event.getDamage(), DamageType.WEAPON, DamageType.PHYSICAL), playerData.getMMOPlayerData().getStatMap().cache(EquipmentSlot.OFF_HAND));
		attack.applyEffects(offhandItem, target);
		event.setDamage(attack.getDamage().getDamage());
	}
}
