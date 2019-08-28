package net.Indyuce.mmoitems.listener.version;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.interaction.util.InteractItem;
import net.Indyuce.mmoitems.api.interaction.weapon.Weapon;
import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.api.player.PlayerData;

public class Listener_v1_13 implements Listener {
	@EventHandler
	public void a(ProjectileLaunchEvent event) {
		if (!(event.getEntity() instanceof Trident) || !(event.getEntity().getShooter() instanceof Player))
			return;

		InteractItem item = new InteractItem((Player) event.getEntity().getShooter(), Material.TRIDENT);
		if (!item.hasItem())
			return;

		NBTItem nbtItem = MMOItems.plugin.getNMS().getNBTItem(item.getItem());
		Type type = nbtItem.getType();

		PlayerData playerData = PlayerData.get((Player) event.getEntity().getShooter());
		if (type != null)
			if (!new Weapon(playerData, nbtItem, type).canBeUsed()) {
				event.setCancelled(true);
				return;
			}

		MMOItems.plugin.getEntities().registerCustomProjectile(nbtItem, playerData.getStats().newTemporary(), (Trident) event.getEntity(), type != null);
	}
}
