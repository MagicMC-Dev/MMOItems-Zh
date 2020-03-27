package net.Indyuce.mmoitems.comp.holograms;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.sainttx.holograms.HologramPlugin;
import com.sainttx.holograms.api.Hologram;
import com.sainttx.holograms.api.HologramManager;
import com.sainttx.holograms.api.line.TextLine;

import net.Indyuce.mmoitems.MMOItems;

public class HologramsPlugin extends HologramSupport {
	private HologramManager hologramManager = JavaPlugin.getPlugin(HologramPlugin.class).getHologramManager();

	public HologramsPlugin() {
		super();
	}

	@Override
	public void displayIndicator(Location loc, String message, Player player) {
		Hologram hologram = new Hologram("MMOItems_" + UUID.randomUUID().toString(), loc);
		hologramManager.addActiveHologram(hologram);
		hologram.addLine(new TextLine(hologram, message));
		Bukkit.getScheduler().scheduleSyncDelayedTask(MMOItems.plugin, () -> hologramManager.deleteHologram(hologram), 20);
	}
}
