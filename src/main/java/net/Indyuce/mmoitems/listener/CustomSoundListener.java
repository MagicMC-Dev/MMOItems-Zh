package net.Indyuce.mmoitems.listener;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.item.NBTItem;

public class CustomSoundListener implements Listener {
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void a(EntityDamageByEntityEvent event) {
		if(!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof LivingEntity))
			return;
		
		Player player = (Player) event.getDamager();
		playSound(player.getInventory().getItemInMainHand(), "ON_ATTACK", player);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void b(BlockBreakEvent event) {
		playSound(event.getPlayer().getInventory().getItemInMainHand(), "ON_BLOCK_BREAK", event.getPlayer());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void b(PlayerInteractEvent event) {
		if(event.getAction().equals(Action.RIGHT_CLICK_AIR)
				|| event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
		playSound(event.getItem(), "ON_RIGHT_CLICK", event.getPlayer());
	}
	
	void playSound(ItemStack item, String sound, Player player) {
		if(item == null) return;
		NBTItem nbt = NBTItem.get(item);
		if(nbt.hasTag("MMOITEMS_" + sound))
		{
			player.playSound(player.getLocation(),
					nbt.getString("MMOITEMS_" + sound),
					(float) nbt.getDouble("MMOITEMS_" + sound + "_VOL"),
					(float) nbt.getDouble("MMOITEMS_" + sound + "_PIT"));
		}
	}
}
