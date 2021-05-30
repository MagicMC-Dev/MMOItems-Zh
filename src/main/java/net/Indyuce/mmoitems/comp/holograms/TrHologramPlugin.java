package net.Indyuce.mmoitems.comp.holograms;

import me.arasple.mc.trhologram.api.TrHologramAPI;
import me.arasple.mc.trhologram.module.display.Hologram;
import net.Indyuce.mmoitems.MMOItems;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 *
 * updated comp provided through discord
 * @author TUCAOEVER
 */
public class TrHologramPlugin extends HologramSupport
{
	@Override
	public void displayIndicator(final Location loc, final String format, final Player player) {
		Hologram hologram = TrHologramAPI.builder(loc)
				.append(format)
				.build();
		Bukkit.getScheduler().scheduleSyncDelayedTask(MMOItems.plugin, hologram::destroy, 20L);
	}
}
