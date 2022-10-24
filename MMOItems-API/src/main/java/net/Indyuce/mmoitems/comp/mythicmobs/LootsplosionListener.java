package net.Indyuce.mmoitems.comp.mythicmobs;

import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.util.MMOUtils;
import net.Indyuce.mmoitems.api.ItemTier;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LootsplosionListener implements Listener {
    private static final Random random = new Random();

    private final boolean colored;

    public LootsplosionListener() {
        colored = MMOItems.plugin.getConfig().getBoolean("lootsplosion.color");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void b(MythicMobDeathEvent event) {
        if (event.getMob().getVariables().has("Lootsplosion"))
            new LootsplosionHandler(event);
    }

    public class LootsplosionHandler implements Listener {
        private final List<ItemStack> drops;

        /*
         * Y coordinate offset so the velocity is not directly negated when the
         * item spawns on the ground
         */
        private final double offset;

        public LootsplosionHandler(MythicMobDeathEvent event) {
            offset = event.getEntity().getHeight() / 2;
            drops = new ArrayList<>(event.getDrops());

            Bukkit.getPluginManager().registerEvents(this, MMOItems.plugin);
        }

        private void close() {
            ItemSpawnEvent.getHandlerList().unregister(this);
        }

        @EventHandler
        public void a(ItemSpawnEvent event) {
            Item item = event.getEntity();
            if (!drops.contains(item.getItemStack())) {
                close();
                return;
            }

            drops.remove(item.getItemStack());
            item.teleport(item.getLocation().add(0, offset, 0));
            item.setVelocity(randomVector());

            if (colored)
                Bukkit.getScheduler().runTask(MMOItems.plugin, () -> {
                    NBTItem nbt = MythicLib.plugin.getVersion().getWrapper().getNBTItem(item.getItemStack());
                    if (nbt.hasTag("MMOITEMS_TIER")) {
                        ItemTier tier = MMOItems.plugin.getTiers().get(nbt.getString("MMOITEMS_TIER"));
                        if (tier.hasColor())
                            new LootColor(item, tier.getColor());
                    }
                });
        }
    }

    private Vector randomVector() {
        double offset = MMOItems.plugin.getConfig().getDouble("lootsplosion.offset"),
                height = MMOItems.plugin.getConfig().getDouble("lootsplosion.height");
        return new Vector(Math.cos(random.nextDouble() * Math.PI * 2) * offset, height, Math.sin(random.nextDouble() * Math.PI * 2) * offset);
    }

    public class LootColor extends BukkitRunnable {
        private final Item item;
        private final Color color;

        private int j = 0;

        public LootColor(Item item, ChatColor color) {
            this.item = item;
            this.color = MMOUtils.toRGB(color);

            runTaskTimer(MMOItems.plugin, 0, 1);
        }

        @Override
        public void run() {
            if (j++ > 100 || item.isDead() || item.isOnGround()) {
                cancel();
                return;
            }

            item.getWorld().spawnParticle(Particle.REDSTONE, item.getLocation(), 1, new Particle.DustOptions(color, 1.3f));
        }
    }
}
