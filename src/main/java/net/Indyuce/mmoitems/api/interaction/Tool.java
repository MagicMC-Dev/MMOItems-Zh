package net.Indyuce.mmoitems.api.interaction;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.comp.flags.CustomFlag;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.event.BouncingCrackBlockBreakEvent;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Map;

public class Tool extends UseItem {
    public Tool(Player player, NBTItem item) {
        super(player, item);
    }

    @Override
    public boolean checkItemRequirements() {
        return MythicLib.plugin.getFlags().isFlagAllowed(player, CustomFlag.MI_TOOLS) && playerData.getRPG().canUse(getNBTItem(), true);
    }

    /**
     * @param block Block being broken
     * @return If the mining event should be canceled
     */
    public boolean miningEffects(Block block) {
        boolean cancel = false;

        if (getNBTItem().getBoolean("MMOITEMS_AUTOSMELT")) {
            Map<Material, Material> oreDrops = MythicLib.plugin.getVersion().getWrapper().getOreDrops();
            Material drop = oreDrops.get(block.getType());
            if (drop != null) {
                UtilityMethods.dropItemNaturally(block.getLocation(), new ItemStack(drop));
                block.getWorld().spawnParticle(Particle.CLOUD, block.getLocation().add(.5, .5, .5), 0);
                block.setType(Material.AIR);
                cancel = true;
            }
        }

        if (getNBTItem().getBoolean("MMOITEMS_BOUNCING_CRACK") && !getPlayerData().isOnCooldown(PlayerData.CooldownType.BOUNCING_CRACK)) {
            getPlayerData().applyCooldown(PlayerData.CooldownType.BOUNCING_CRACK, 1);
            new BukkitRunnable() {
                final Vector v = player.getEyeLocation().getDirection().multiply(.5);
                final Location loc = block.getLocation().clone().add(.5, .5, .5);
                int j = 0;

                public void run() {
                    if (j++ > 10)
                        cancel();

                    loc.add(v);
                    Block block = loc.getBlock();
                    if (block.getType() == Material.AIR || MMOItems.plugin.getLanguage().isBlacklisted(block.getType()))
                        return;

                    BlockBreakEvent breakEvent = new BouncingCrackBlockBreakEvent(block, player);
                    Bukkit.getPluginManager().callEvent(breakEvent);
                    if (breakEvent.isCancelled()) {
                        cancel();
                        return;
                    }

                    block.breakNaturally(getItem());
                    loc.getWorld().playSound(loc, Sound.BLOCK_GRAVEL_BREAK, 1, 1);
                }
            }.runTaskTimer(MMOItems.plugin, 0, 1);
        }
			
		/*if (getNBTItem().hasTag("MMOITEMS_BREAK_SIZE")) {
			int breakSize = getNBTItem().getInteger("MMOITEMS_BREAK_SIZE");
			if(breakSize % 2 != 0) {
				BlockFace face = player.getFacing();
				System.out.println("Debug: Facing - " + face);
			}
		}*/
        return cancel;
    }
}
