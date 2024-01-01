package net.Indyuce.mmoitems.api.interaction;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.comp.flags.CustomFlag;
import io.lumine.mythic.lib.version.OreDrops;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.event.BouncingCrackBlockBreakEvent;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

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
    public boolean miningEffects(@NotNull Block block) {
        boolean cancel = false;

        if (getNBTItem().getBoolean("MMOITEMS_AUTOSMELT")) {
            final OreDrops drops = MythicLib.plugin.getVersion().getWrapper().getOreDrops(block.getType());
            if (drops != null) {
                UtilityMethods.dropItemNaturally(block.getLocation(), drops.generate(getFortuneLevel()));
                block.getWorld().spawnParticle(Particle.LAVA, block.getLocation().add(.5, .5, .5), 4);
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

        return cancel;
    }

    private int getFortuneLevel() {
        if (!getItem().hasItemMeta()) return 0;
        return getItem().getItemMeta().getEnchantLevel(Enchantment.LOOT_BONUS_BLOCKS);
    }
}
