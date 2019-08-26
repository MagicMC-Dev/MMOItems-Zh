package net.Indyuce.mmoitems.api;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Arrow;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.NBTItem;

public class ArrowParticles extends BukkitRunnable {
	private Arrow arrow;

	private Particle particle;
	private int amount;
	private float offset, speed;
	private Color color;

	private boolean valid = true, colored = false;

	public ArrowParticles(Arrow arrow) {
		this.arrow = arrow;
	}

	public ArrowParticles load(NBTItem item) {
		String tag = item.getString("MMOITEMS_ARROW_PARTICLES");
		if (tag.equals("")) {
			valid = false;
			return this;
		}

		JsonObject object = new JsonParser().parse(tag).getAsJsonObject();

		particle = Particle.valueOf(object.get("Particle").getAsString());
		amount = object.get("Amount").getAsInt();
		offset = (float) object.get("Offset").getAsDouble();

		if (colored = object.get("Colored").getAsBoolean())
			color = Color.fromRGB(object.get("Red").getAsInt(), object.get("Green").getAsInt(), object.get("Blue").getAsInt());
		else
			speed = object.get("Speed").getAsFloat();

		return this;
	}

	public boolean isValid() {
		return valid;
	}

	@Override
	public void run() {
		if (arrow.isDead() || arrow.isOnGround()) {
			cancel();
			return;
		}

		if (colored)
			MMOItems.plugin.getVersion().getVersionWrapper().spawnParticle(particle, arrow.getLocation().add(0, .25, 0), amount, offset, offset, offset, 0, 1, color);
		else
			arrow.getWorld().spawnParticle(particle, arrow.getLocation().add(0, .25, 0), amount, offset, offset, offset, speed);
	}
}
