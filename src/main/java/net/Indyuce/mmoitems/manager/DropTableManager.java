package net.Indyuce.mmoitems.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.drop.DropTable;

public class DropTableManager implements Listener {
	private Map<EntityType, DropTable> monsters = new HashMap<>();
	private Map<Material, DropTable> blocks = new HashMap<>();

	public DropTableManager() {
		reload();
	}

	public void reload() {
		FileConfiguration config = new ConfigFile("drops").getConfig();

		monsters.clear();
		blocks.clear();

		if (config.contains("monsters"))
			for (String key : config.getConfigurationSection("monsters").getKeys(false))
				try {
					EntityType type = EntityType.valueOf(key.toUpperCase().replace("-", "_").replace(" ", "_"));
					monsters.put(type, new DropTable(config.getConfigurationSection("monsters." + key)));
				} catch (Exception e) {
					MMOItems.plugin.getLogger().log(Level.WARNING, "Couldn't read the drop table mob type " + key);
				}

		if (config.contains("blocks"))
			for (String key : config.getConfigurationSection("blocks").getKeys(false))
				try {
					Material material = Material.valueOf(key.toUpperCase().replace("-", "_").replace(" ", "_"));
					blocks.put(material, new DropTable(config.getConfigurationSection("blocks." + key)));
				} catch (Exception e) {
					MMOItems.plugin.getLogger().log(Level.WARNING, "Couldn't read the drop table material " + key);
				}
	}

	@EventHandler
	public void a(EntityDeathEvent event) {
		LivingEntity entity = event.getEntity();
		if (monsters.containsKey(entity.getType()))
			event.getDrops().addAll(monsters.get(entity.getType()).read(false));
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void b(BlockBreakEvent event) {
		if (event.isCancelled())
			return;

		Player player = event.getPlayer();
		if (player == null || player.getGameMode() == GameMode.CREATIVE)
			return;

		Block block = event.getBlock();
		final Material type = block.getType();
		if (blocks.containsKey(type))
			Bukkit.getScheduler().runTaskLater(MMOItems.plugin, () -> {
				for (ItemStack drop : blocks.get(type).read(hasSilkTouchTool(player)))
					block.getWorld().dropItemNaturally(block.getLocation().add(.5, .3, .5), drop);
			}, 1);
	}

	private boolean hasSilkTouchTool(Player player) {
		ItemStack item = player.getInventory().getItemInMainHand();
		return item != null && item.getType() != Material.AIR && item.hasItemMeta() && item.getItemMeta().hasEnchant(Enchantment.SILK_TOUCH);
	}
}