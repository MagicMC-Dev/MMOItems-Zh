package net.Indyuce.mmoitems.api.interaction.projectile;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.lumine.mythic.lib.player.particle.ParticleInformation;
import net.Indyuce.mmoitems.MMOItems;
import io.lumine.mythic.lib.api.item.NBTItem;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Arrow;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @deprecated Merge with {@link ParticleInformation}
 */
@Deprecated
public class ArrowParticles extends BukkitRunnable {
	private final Arrow arrow;

	private final Particle particle;
	private final int amount;
	private final float offset, speed;
	private final Color color;

	public ArrowParticles(Arrow arrow, NBTItem item) {
		this.arrow = arrow;

		JsonObject object = new JsonParser().parse(item.getString("MMOITEMS_ARROW_PARTICLES")).getAsJsonObject();
		particle = Particle.valueOf(object.get("Particle").getAsString());
		amount = object.get("Amount").getAsInt();
		offset = (float) object.get("Offset").getAsDouble();

		boolean colored = object.get("Colored").getAsBoolean();
		color = colored ? Color.fromRGB(object.get("Red").getAsInt(), object.get("Green").getAsInt(), object.get("Blue").getAsInt()) : null;
		speed = colored ? 0 : object.get("Speed").getAsFloat();

		runTaskTimer(MMOItems.plugin, 0, 1);
	}

	@Override
	public void run() {
		if (arrow.isDead() || arrow.isOnGround()) {
			cancel();
			return;
		}

		// TODO Allow Note to be colored and allow BLOCK_DUST/ITEM_DUST to pick a block/item.
		if (color != null) {
			if (particle == Particle.REDSTONE) {
				arrow.getWorld().spawnParticle(particle, arrow.getLocation().add(0, .25, 0), amount, offset, offset, offset, new Particle.DustOptions(color, 1));
			} else if (particle == Particle.SPELL_MOB || particle == Particle.SPELL_MOB_AMBIENT) {
				// 0 for amount to allow colors (Thats why there is a for loop). Then the offsets are RGB values from 0.0 - 1.0, last 1 is the brightness.
				for (int i = 0; i < amount; i++) {
					arrow.getWorld().spawnParticle(particle, arrow.getLocation().add(0, .25, 0), 0, (float) color.getRed() / 255, (float) color.getGreen() / 255, (float) color.getBlue() / 255, 1);
				}
			}
			// else if (particle == Particle.NOTE) { Do Fancy Color Stuff Good Luck } 
			// The above code semi-worked for note particles but I think there is a limited amount of colors so its harder and prob have to get the nearest one.
		} else {
			arrow.getWorld().spawnParticle(particle, arrow.getLocation().add(0, .25, 0), amount, offset, offset, offset, speed);
		}
	}
}
