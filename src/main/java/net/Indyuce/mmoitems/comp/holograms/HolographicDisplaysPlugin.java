package net.Indyuce.mmoitems.comp.holograms;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

import net.Indyuce.mmoitems.MMOItems;

public class HolographicDisplaysPlugin extends HologramSupport {
	public HolographicDisplaysPlugin() {
		super();
	}

	@Override
	public void displayIndicator(Location loc, String format, Player player) {
		Hologram hologram = HologramsAPI.createHologram(MMOItems.plugin, loc);
		hologram.appendTextLine(format);
		if (player != null)
			hologram.getVisibilityManager().hideTo(player);
		Bukkit.getScheduler().scheduleSyncDelayedTask(MMOItems.plugin, hologram::delete, 20);
	}
}
