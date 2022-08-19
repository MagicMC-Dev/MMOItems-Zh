package net.Indyuce.mmoitems.listener.option;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemTier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class DroppedItems implements Listener {
    private final boolean tierGlow, hints;

    public DroppedItems(ConfigurationSection config) {
        tierGlow = config.getBoolean("tier-glow");
        hints = config.getBoolean("hints");
    }

    /**
     * Applies both item hints & item glow
     * depending on the tier of the item dropped.
     */
    @EventHandler
    public void applyOnSpawn(ItemSpawnEvent event) {
        final ItemStack item = event.getEntity().getItemStack();
        final @Nullable ItemTier tier = MMOItems.plugin.getTiers().get(NBTItem.get(item).getString("MMOITEMS_TIER"));
        if (tier == null)
            return;

        if (hints && tier.isHintEnabled()) {
            event.getEntity().setCustomNameVisible(true);
            event.getEntity().setCustomName(item.getItemMeta().getDisplayName());
        }

        if (tierGlow && tier.hasColor())
            MythicLib.plugin.getGlowing().setGlowing(event.getEntity(), tier.getColor());
    }
}
