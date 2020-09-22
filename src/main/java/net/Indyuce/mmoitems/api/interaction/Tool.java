package net.Indyuce.mmoitems.api.interaction;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.comp.flags.FlagPlugin.CustomFlag;
import net.mmogroup.mmolib.api.item.NBTItem;

public class Tool extends UseItem {
	public Tool(Player player, NBTItem item) {
		super(player, item);
	}

	@Override
	public boolean canBeUsed() {
		return MMOItems.plugin.getFlags().isFlagAllowed(player, CustomFlag.MI_TOOLS) && playerData.getRPG().canUse(getNBTItem(), true);
	}

	public boolean miningEffects(Block block) {
		boolean cancel = false;
		
		if (nbt.getBoolean("MMOITEMS_AUTOSMELT"))
			if (block.getType() == Material.IRON_ORE || block.getType() == Material.GOLD_ORE) {
				ItemStack item = new ItemStack(Material.valueOf(block.getType().name().replace("_ORE", "") + "_INGOT"));

				Location loc = block.getLocation().add(.5, 0, .5);
				block.setType(Material.AIR);
				block.getWorld().dropItemNaturally(loc, item);
				block.getWorld().spawnParticle(Particle.CLOUD, loc.add(0, .5, 0), 0);
				cancel = true;
			}

		if (nbt.getBoolean("MMOITEMS_BOUNCING_CRACK"))
			new BukkitRunnable() {
				Vector v = player.getEyeLocation().getDirection().multiply(.5);
				Location loc = block.getLocation().clone().add(.5, .5, .5);
				int j = 0;

				public void run() {
					j++;
					if (j > 10)
						cancel();

					loc.add(v);
					Block block = loc.getBlock();
					if (block.getType() == Material.AIR || MMOItems.plugin.getLanguage().isBlacklisted(block.getType()))
						return;

					block.breakNaturally(getItem());
					loc.getWorld().playSound(loc, Sound.BLOCK_GRAVEL_BREAK, 1, 1);
				}
			}.runTaskTimer(MMOItems.plugin, 0, 1);
			
		if (nbt.hasTag("MMOITEMS_BREAK_SIZE")) {
			int breakSize = nbt.getInteger("MMOITEMS_BREAK_SIZE");
			if(breakSize % 2 != 0) {
				BlockFace face = player.getFacing();
				System.out.println("Debug: Facing - " + face);
			}
		}
		return cancel;
	}
}
