package net.Indyuce.mmoitems.comp.holograms;

import java.util.Arrays;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Modules.Holograms.CMIHologram;

import net.Indyuce.mmoitems.MMOItems;

public class CMIPlugin extends HologramSupport {
	public CMIPlugin() {
		super();
	}

	@Override
	public void displayIndicator(Location loc, String format, Player player) {
		final CMIHologram hologram = new CMIHologram("MMOItems_" + UUID.randomUUID().toString(), loc);
		hologram.setLines(Arrays.asList(format));
		if (player != null)
			hologram.hide(player.getUniqueId());
		CMI.getInstance().getHologramManager().addHologram(hologram);
		hologram.update();

		Bukkit.getScheduler().scheduleSyncDelayedTask(MMOItems.plugin, () -> CMI.getInstance().getHologramManager().removeHolo(hologram), 20);
	}
}
