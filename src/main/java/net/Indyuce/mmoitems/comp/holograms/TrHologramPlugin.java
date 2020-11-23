package net.Indyuce.mmoitems.comp.holograms;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import me.arasple.mc.trhologram.api.TrHologramAPI;
import me.arasple.mc.trhologram.hologram.Hologram;
import net.Indyuce.mmoitems.MMOItems;

public class TrHologramPlugin extends HologramSupport {
	public TrHologramPlugin() {
		super();
	}

	@Override
	public void displayIndicator(Location loc, String format, Player player) {
		Hologram hologram = TrHologramAPI.createHologram(MMOItems.plugin, "mmoitems-"  + UUID.randomUUID().toString(), loc, format);
		Bukkit.getScheduler().scheduleSyncDelayedTask(MMOItems.plugin, hologram::delete, 20);
	}
}
