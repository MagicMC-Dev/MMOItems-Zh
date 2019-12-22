package net.Indyuce.mmoitems.listener;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.event.ItemBreakEvent;
import net.mmogroup.mmolib.api.item.NBTItem;

public class CustomSoundListener implements Listener {
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void a(EntityDamageByEntityEvent event) {
		if(!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof LivingEntity))
			return;
		
		Player player = (Player) event.getDamager();
		playSound(player.getInventory().getItemInMainHand(), "ON_ATTACK", player);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void b(EntityPickupItemEvent event) {
    	if(event.getEntityType().equals(EntityType.PLAYER))
    		playSound(event.getItem().getItemStack(), "ON_PICKUP", (Player) event.getEntity());
    }
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void c(BlockBreakEvent event) {
		playSound(event.getPlayer().getInventory().getItemInMainHand(), "ON_BLOCK_BREAK", event.getPlayer());
	}

	@EventHandler
	public void d(PlayerInteractEvent event) {
		if(event.getHand() == null || event.getHand() == EquipmentSlot.OFF_HAND || !event.hasItem()) return;
		
		if(event.hasBlock())
		{
			if(event.getAction().name().contains("RIGHT_CLICK"))
				playSound(event.getItem(), "ON_RIGHT_CLICK", event.getPlayer());

			if(event.getAction().name().contains("LEFT_CLICK"))
				playSound(event.getItem(), "ON_LEFT_CLICK", event.getPlayer());
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void e(CraftItemEvent event) {
    	playSound(event.getInventory().getResult(), "ON_CRAFT",
    			event.getWhoClicked().getWorld(), event.getWhoClicked().getLocation());
    }

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void f(FurnaceSmeltEvent event) {
		playSound(event.getResult(), "ON_CRAFT", event.getBlock());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void g(PlayerItemConsumeEvent event) {
    	playSound(event.getItem(), "ON_CONSUME", event.getPlayer());
    }

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void h1(PlayerItemBreakEvent event) {
    	playSound(event.getBrokenItem(), "ON_ITEM_BREAK", event.getPlayer());
    }
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void h2(ItemBreakEvent event) {
    	playSound(event.getItem().getItem(), "ON_ITEM_BREAK", event.getPlayer());
    }

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void i(BlockPlaceEvent event) {
    	playSound(event.getItemInHand(), "ON_PLACED", event.getPlayer());
    }
	
	public static void stationCrafting(ItemStack item, Player player) {
		if(item == null) return;
		NBTItem nbt = NBTItem.get(item);
		if(nbt.hasTag("MMOITEMS_SOUND_ON_CRAFT")) {
			player.getWorld().playSound(player.getLocation(),
					nbt.getString("MMOITEMS_SOUND_ON_CRAFT"),
					(float) nbt.getDouble("MMOITEMS_SOUND_ON_CRAFT_VOL"),
					(float) nbt.getDouble("MMOITEMS_SOUND_ON_CRAFT_PIT"));
		}
	}

	void playSound(ItemStack item, String sound, Player player)
	{ playSound(item, sound, player.getWorld(), player.getLocation()); }
	void playSound(ItemStack item, String sound, Block block)
	{ playSound(item, sound, block.getWorld(), block.getLocation()); }

	void playSound(ItemStack item, String sound, World world, Location loc) {
		if(item == null) return;
		NBTItem nbt = NBTItem.get(item);
		if(nbt.hasTag("MMOITEMS_SOUND_" + sound)) {
			world.playSound(loc, nbt.getString("MMOITEMS_SOUND_" + sound),
					(float) nbt.getDouble("MMOITEMS_SOUND_" + sound + "_VOL"),
					(float) nbt.getDouble("MMOITEMS_SOUND_" + sound + "_PIT"));
		}
	}
}
