package net.Indyuce.mmoitems.stat.data;

import com.google.gson.JsonObject;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import org.bukkit.Particle;

public class ArrowParticlesData implements StatData, RandomStatData<ArrowParticlesData> {
	private final Particle particle;
	private final int amount, red, green, blue;
	private final double speed, offset;
	private final boolean colored;

	public ArrowParticlesData(Particle particle, int amount, double offset, double speed) {
		this.particle = particle;
		this.amount = amount;
		this.offset = offset;

		this.speed = speed;

		this.colored = false;
		this.red = 0;
		this.blue = 0;
		this.green = 0;
	}

	public ArrowParticlesData(Particle particle, int amount, double offset, int red, int green, int blue) {
		this.particle = particle;
		this.amount = amount;
		this.offset = offset;

		this.speed = 0;

		this.colored = true;
		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	public Particle getParticle() {
		return particle;
	}

	public boolean isColored() {
		return colored;
	}

	public int getAmount() {
		return amount;
	}

	public double getOffset() {
		return offset;
	}

	public double getSpeed() {
		return speed;
	}

	public int getRed() {
		return red;
	}

	public int getGreen() {
		return green;
	}

	public int getBlue() {
		return blue;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public String toString() {
		JsonObject object = new JsonObject();
		object.addProperty("Particle", particle.name());
		object.addProperty("Amount", amount);
		object.addProperty("Offset", offset);
		object.addProperty("Colored", colored);
		if (colored) {
			object.addProperty("Red", red);
			object.addProperty("Green", green);
			object.addProperty("Blue", blue);
		} else
			object.addProperty("Speed", speed);

		return object.toString();
	}

	@Override
	public ArrowParticlesData randomize(MMOItemBuilder builder) {
		return this;
	}
}