package net.Indyuce.mmoitems.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
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
import net.Indyuce.mmoitems.api.CustomBlock;
import net.Indyuce.mmoitems.api.droptable.DropTable;
import net.Indyuce.mmoitems.api.event.blocks.CustomBlockDropEvent;
import net.Indyuce.mmoitems.listener.CustomBlockListener;
import net.mmogroup.mmolib.MMOLib;

public class DropTableManager implements Listener {
	private final Map<EntityType, DropTable> monsters = new HashMap<>();
	private final Map<Material, DropTable> blocks = new HashMap<>();
	private final Map<Integer, DropTable> customBlocks = new HashMap<>();

	public DropTableManager() {
		reload();
	}

	public void reload() {
		monsters.clear();
		blocks.clear();
		customBlocks.clear();

		FileConfiguration config = new ConfigFile("drops").getConfig();
		if (config.contains("monsters"))
			for (String key : config.getConfigurationSection("monsters").getKeys(false))
				try {
					EntityType type = EntityType.valueOf(key.toUpperCase().replace("-", "_").replace(" ", "_"));
					monsters.put(type, new DropTable(config.getConfigurationSection("monsters." + key)));
				} catch (IllegalArgumentException exception) {
					MMOItems.plugin.getLogger().log(Level.WARNING,
							"Could not read drop table with mob type '" + key + "': " + exception.getMessage());
				}

		if (config.contains("blocks"))
			for (String key : config.getConfigurationSection("blocks").getKeys(false))
				try {
					Material material = Material.valueOf(key.toUpperCase().replace("-", "_").replace(" ", "_"));
					blocks.put(material, new DropTable(config.getConfigurationSection("blocks." + key)));
				} catch (IllegalArgumentException exception) {
					MMOItems.plugin.getLogger().log(Level.WARNING,
							"Could not read drop table with material '" + key + "': " + exception.getMessage());
				}

		if (config.contains("customblocks"))
			for (String key : config.getConfigurationSection("customblocks").getKeys(false))
				try {
					int id = Integer.parseInt(key);
					customBlocks.put(id, new DropTable(config.getConfigurationSection("customblocks." + key)));
				} catch (IllegalArgumentException exception) {
					MMOItems.plugin.getLogger().log(Level.WARNING,
							"Could not read drop table with custom block '" + key + "': " + exception.getMessage());
				}
	}

	@EventHandler
	public void a(EntityDeathEvent event) {
		LivingEntity entity = event.getEntity();
		if (monsters.containsKey(entity.getType()))
			event.getDrops().addAll(monsters.get(entity.getType()).read(false));
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void b(BlockBreakEvent event) {
		Player player = event.getPlayer();
		if (player == null || player.getGameMode() == GameMode.CREATIVE)
			return;

		Block block = event.getBlock();
		Optional<CustomBlock> opt = MMOLib.plugin.getVersion().isStrictlyHigher(1, 12)
				? MMOItems.plugin.getCustomBlocks().getFromBlock(block.getBlockData())
				: Optional.empty();

		if (opt.isPresent()) {
			CustomBlock customBlock = opt.get();

			/*
			 * check if corresponding custom block has a drop table registered,
			 * and only reads corresponding drop table if the tool has enough
			 * power
			 */
			if (customBlocks.containsKey(customBlock.getId()) && CustomBlockListener.getPickaxePower(player) >= customBlock.getRequiredPower())
				Bukkit.getScheduler().runTaskLater(MMOItems.plugin, () -> {
					List<ItemStack> drops = customBlocks.get(customBlock.getId()).read(hasSilkTouchTool(player));
					CustomBlockDropEvent called = new CustomBlockDropEvent(player, customBlock, drops);
					Bukkit.getPluginManager().callEvent(called);
					if (called.isCancelled())
						return;

					for (ItemStack drop : drops) {
						Item item = block.getWorld().dropItemNaturally(block.getLocation().add(.5, .1, .5), drop);
						item.setVelocity(item.getVelocity().multiply(0.5f));
					}
				}, 2);
			return;
		}

		if (blocks.containsKey(block.getType())) {
			final Material type = block.getType();
			Bukkit.getScheduler().runTaskLater(MMOItems.plugin, () -> {
				for (ItemStack drop : blocks.get(type).read(hasSilkTouchTool(player))) {
					Item item = block.getWorld().dropItemNaturally(block.getLocation().add(.5, .1, .5), drop);
					item.setVelocity(item.getVelocity().multiply(0.5f));
				}
			}, 2);
		}
	}

	// public Collection<ItemStack> getBlockDrops(Block block, Player player) {
	// final Material type = block.getType();
	//
	// CustomBlock custom = MMOLib.plugin.getVersion().isStrictlyHigher(1, 12) ?
	// CustomBlock.getFromData(block.getBlockData()) : null;
	// if (custom != null) {
	// if (customBlocks.containsKey(custom.getId())) {
	// if (CustomBlockListener.getPickaxePower(player) >=
	// custom.getRequiredPower()) {
	// return customBlocks.get(custom.getId()).read(hasSilkTouchTool(player));
	// }
	// }
	// } else {
	// if (blocks.containsKey(type)) {
	// return blocks.get(type).read(hasSilkTouchTool(player));
	// }
	// }
	// return block.getDrops(player.getInventory().getItemInMainHand());
	// }

	public boolean hasSilkTouchTool(Player player) {
		ItemStack item = player.getInventory().getItemInMainHand();
		return item != null && item.getType() != Material.AIR && item.hasItemMeta() && item.getItemMeta().hasEnchant(Enchantment.SILK_TOUCH);
	}
}