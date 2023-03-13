package net.Indyuce.mmoitems.tasks;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.block.CustomBlock;
import net.Indyuce.mmoitems.manager.WorldGenManager;
import net.Indyuce.mmoitems.util.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Queue;

/**
 * mmoitems
 * 13/03/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class CustomBlocksPopulateTask extends BukkitRunnable {

    private final WorldGenManager manager;
    private boolean running = false;

    public CustomBlocksPopulateTask(WorldGenManager manager) {
        this.manager = manager;
    }

    @Override
    public void run() {
        final Queue<Pair<Location, CustomBlock>> modificationsQueue = manager.getModificationsQueue();
        final Pair<Location, CustomBlock> pair = modificationsQueue.poll();

        // If the queue is empty, cancel the task
        if (pair == null) {
            this.stop();
            return;
        }

        // If the chunk is not loaded, skip it
        if (!pair.getKey().getChunk().isLoaded())
            return;

        // If the block is already modified, skip it
        if (pair.getKey().getBlock().getBlockData().equals(pair.getValue().getState().getBlockData()))
            return;

        // Change the block
        Bukkit.getScheduler().runTask(MMOItems.plugin, () -> setBlockData(pair.getKey().getBlock(), pair.getValue()));
    }

    private void setBlockData(Block fModify, CustomBlock block) {
        fModify.setType(block.getState().getType(), false);
        fModify.setBlockData(block.getState().getBlockData(), false);
    }

    public void start() {
        if (running) return;
        running = true;
        this.runTaskTimerAsynchronously(MMOItems.plugin, 0, 1);
    }

    public void stop() {
        if (!running) return;
        running = false;
        this.cancel();
    }

    public boolean isRunning() {
        return running;
    }
}
