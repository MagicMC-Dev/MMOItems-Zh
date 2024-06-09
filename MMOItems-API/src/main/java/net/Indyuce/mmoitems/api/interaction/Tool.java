package net.Indyuce.mmoitems.api.interaction;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.comp.flags.CustomFlag;
import io.lumine.mythic.lib.math3.geometry.euclidean.threed.Line;
import io.lumine.mythic.lib.math3.geometry.euclidean.threed.Vector3D;
import io.lumine.mythic.lib.version.OreDrops;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.event.BouncingCrackBlockBreakEvent;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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
                UtilityMethods.dropItemNaturally(block.getLocation(), drops.generate(getFortuneLevel()));
                block.getWorld().spawnParticle(Particle.LAVA, block.getLocation().add(.5, .5, .5), 4);
                block.setType(Material.AIR);
                cancel = true;
            }
        }

        if (getNBTItem().getBoolean("MMOITEMS_BOUNCING_CRACK") && !getPlayerData().isOnCooldown(PlayerData.CooldownType.BOUNCING_CRACK)) {
            getPlayerData().applyCooldown(PlayerData.CooldownType.BOUNCING_CRACK, 1);
            new BukkitRunnable() {
                final Vector globalDirection = player.getEyeLocation().getDirection();
                final Vector3D sourcePoint = toJava(block.getLocation().add(.5, .5, .5).toVector());
                final Line line = new Line(sourcePoint, sourcePoint.add(toJava(globalDirection)), 1e-10);
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
                    curr.getWorld().playSound(curr.getLocation(), Sound.BLOCK_GRAVEL_BREAK, 1, 1);
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
                    final Vector3D center = toJava(candidateBlock.getLocation().add(.5, .5, .5).toVector());
                    return line.distance(center) - products[candidate.ordinal()];
                }

            }.runTaskTimer(MMOItems.plugin, 0, 1);
        }

        return cancel;
    }

    private int getFortuneLevel() {
        if (!getItem().hasItemMeta()) return 0;
        return getItem().getItemMeta().getEnchantLevel(Enchantment.LOOT_BONUS_BLOCKS);
    }

    private Vector3D toJava(Vector vector) {
        return new Vector3D(vector.getX(), vector.getY(), vector.getZ());
    }
}
