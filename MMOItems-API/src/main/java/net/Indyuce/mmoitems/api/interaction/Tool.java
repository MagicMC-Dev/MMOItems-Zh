package net.Indyuce.mmoitems.api.interaction;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.comp.flags.CustomFlag;
import io.lumine.mythic.lib.version.OreDrops;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VEnchantment;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.event.BouncingCrackBlockBreakEvent;
import net.Indyuce.mmoitems.api.interaction.weapon.Weapon;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.util.MMOUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class Tool extends Weapon {
    public Tool(PlayerData playerData, NBTItem item) {
        super(playerData, item);
    }

    @Deprecated
    public Tool(Player player, NBTItem item) {
        super(player, item);
    }

    @Override
    public boolean checkItemRequirements(boolean message) {

        // Light checks first
        if (playerData.isEncumbered()) {
            Message.HANDS_TOO_CHARGED.format(ChatColor.RED).send(getPlayer());
            return false;
        }

        // Check for class, level... then flags
        return playerData.getRPG().canUse(getNBTItem(), message) && flagCheck(MMOItems.plugin.getLanguage().toolFlagChecks, CustomFlag.MI_TOOLS);
    }

    private static final BlockFace[] NEIGHBORS = {BlockFace.NORTH, BlockFace.DOWN, BlockFace.EAST, BlockFace.UP, BlockFace.WEST, BlockFace.SOUTH};

    /**
     * @param block Block being broken
     * @return If the mining event should be canceled
     */
    public boolean miningEffects(@NotNull final Block block) {
        boolean cancel = false;

        if (getNBTItem().getBoolean("MMOITEMS_AUTOSMELT")) {
            final OreDrops drops = MythicLib.plugin.getVersion().getWrapper().getOreDrops(block.getType());
            if (drops != null) {
                final int fortuneLevel = MMOUtils.getLevel(getItem(), VEnchantment.FORTUNE.get());
                UtilityMethods.dropItemNaturally(block.getLocation(), drops.generate(fortuneLevel));
                block.getWorld().spawnParticle(Particle.LAVA, block.getLocation().add(.5, .5, .5), 4);
                block.setType(Material.AIR);
                cancel = true;
            }
        }

        if (getNBTItem().getBoolean("MMOITEMS_BOUNCING_CRACK") && !getPlayerData().isOnCooldown(PlayerData.CooldownType.BOUNCING_CRACK)) {
            getPlayerData().applyCooldown(PlayerData.CooldownType.BOUNCING_CRACK, 1);
            new BukkitRunnable() {
                final Vector globalDirection = player.getEyeLocation().getDirection();
                final Vector point1 = block.getLocation().add(.5, .5, .5).toVector();
                final Vector point2 = point1.clone().add(globalDirection);
                final double[] products = new double[NEIGHBORS.length];

                {
                    for (BlockFace face : NEIGHBORS)
                        products[face.ordinal()] = face.getDirection().dot(globalDirection);
                }

                Block curr = block;
                int j = 0;

                private static final int BLOCKS_BROKEN = 4;

                public void run() {
                    if (++j >= BLOCKS_BROKEN) cancel();

                    curr = findBestBlock();
                    if (curr.getType() == Material.AIR || MMOItems.plugin.getLanguage().isBlacklisted(curr.getType()))
                        return;

                    BlockBreakEvent breakEvent = new BouncingCrackBlockBreakEvent(curr, player);
                    Bukkit.getPluginManager().callEvent(breakEvent);
                    if (breakEvent.isCancelled()) {
                        cancel();
                        return;
                    }

                    curr.breakNaturally(getItem());
                    curr.getWorld().playSound(curr.getLocation(), Sounds.BLOCK_GRAVEL_BREAK, 1, 1);
                }

                @NotNull
                private Block findBestBlock() {
                    Block block = null;
                    double cost = Double.MAX_VALUE;

                    for (BlockFace candidate : NEIGHBORS) {
                        final Block candidateBlock = curr.getRelative(candidate);
                        final double candidateCost = findCost(candidate, candidateBlock);

                        if (candidateCost < cost) {
                            cost = candidateCost;
                            block = candidateBlock;
                        }
                    }

                    return block;
                }

                private double findCost(BlockFace candidate, Block candidateBlock) {
                    final Vector center = candidateBlock.getLocation().add(.5, .5, .5).toVector();
                    return dist(center, point1, point2) - products[candidate.ordinal()];
                }

            }.runTaskTimer(MMOItems.plugin, 0, 1);
        }

        return cancel;
    }

    /**
     * d(A, BC) = norm(BA x BC) / norm(BC)
     *
     * @return Distance from point A to line (BC)
     */
    private double dist(Vector a, Vector b, Vector c) {
        final Vector ab = b.clone().subtract(a);
        final Vector bc = c.clone().subtract(b);
        return ab.getCrossProduct(bc).length() / bc.length();
    }
}
