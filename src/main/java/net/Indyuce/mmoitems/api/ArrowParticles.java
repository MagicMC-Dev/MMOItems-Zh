package net.Indyuce.mmoitems.api;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Arrow;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.Indyuce.mmoitems.MMOItems;
import net.mmogroup.mmolib.api.item.NBTItem;

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

		if (color != null)
			arrow.getWorld().spawnParticle(particle, arrow.getLocation().add(0, .25, 0), amount, offset, offset, offset, color);
		else
			arrow.getWorld().spawnParticle(particle, arrow.getLocation().add(0, .25, 0), amount, offset, offset, offset, speed);
	}
}
