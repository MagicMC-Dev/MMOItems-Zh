package net.Indyuce.mmoitems.particle.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import net.Indyuce.mmoitems.util.MMOUtils;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.StringValue;
import net.Indyuce.mmoitems.particle.AuraParticles;
import net.Indyuce.mmoitems.particle.DoubleRingsParticles;
import net.Indyuce.mmoitems.particle.FirefliesParticles;
import net.Indyuce.mmoitems.particle.GalaxyParticles;
import net.Indyuce.mmoitems.particle.HelixParticles;
import net.Indyuce.mmoitems.particle.OffsetParticles;
import net.Indyuce.mmoitems.particle.VortexParticles;
import net.Indyuce.mmoitems.stat.data.ParticleData;

public enum ParticleType {
	OFFSET(OffsetParticles::new, false, 5, "Some particles randomly spawning around your body.", new StringValue("amount", 5), new StringValue("vertical-offset", .5), new StringValue("horizontal-offset", .3), new StringValue("speed", 0), new StringValue("height", 1)),
	FIREFLIES(FirefliesParticles::new, true, 1, "Particles dashing around you at the same height.", new StringValue("amount", 3), new StringValue("speed", 0), new StringValue("rotation-speed", 1), new StringValue("radius", 1.3), new StringValue("height", 1)),
	VORTEX(VortexParticles::new, true, 1, "Particles flying around you in a cone shape.", new StringValue("radius", 1.5), new StringValue("height", 2.4), new StringValue("speed", 0), new StringValue("y-speed", 1), new StringValue("rotation-speed", 1), new StringValue("amount", 3)),
	GALAXY(GalaxyParticles::new, true, 1, "Particles flying around you in spiral arms.", new StringValue("height", 1), new StringValue("speed", 1), new StringValue("y-coord", 0), new StringValue("rotation-speed", 1), new StringValue("amount", 6)),
	DOUBLE_RINGS(DoubleRingsParticles::new, true, 1, "Particles drawing two rings around you.", new StringValue("radius", .8), new StringValue("y-offset", .4), new StringValue("height", 1), new StringValue("speed", 0), new StringValue("rotation-speed", 1)),
	HELIX(HelixParticles::new, true, 1, "Particles drawing a sphere around you.", new StringValue("radius", .8), new StringValue("height", .6), new StringValue("rotation-speed", 1), new StringValue("y-speed", 1), new StringValue("amount", 4), new StringValue("speed", 0)),
	AURA(AuraParticles::new, true, 1, "Particles dashing around you (height can differ).", new StringValue("amount", 3), new StringValue("speed", 0), new StringValue("rotation-speed", 1), new StringValue("y-speed", 1), new StringValue("y-offset", .7), new StringValue("radius", 1.3), new StringValue("height", 1));

	private final BiFunction<ParticleData, PlayerData, ParticleRunnable> func;
	private final boolean override;
	private final long period;
	private final String lore;
	private final Map<String, Double> modifiers = new HashMap<>();

	/**
	 * @param func
	 *            What the effect does
	 * @param override
	 *            Higher priority particle effect which disables every other
	 *            effects
	 * @param period
	 *            The particle displays again every X seconds
	 * @param lore
	 *            The effect description
	 * @param modifiers
	 *            The list of double modifeirs that allow to configurate the
	 *            particle effect. They are displayed in the effect editor once
	 *            the particle type is chosen
	 */
	ParticleType(BiFunction<ParticleData, PlayerData, ParticleRunnable> func, boolean override, long period, String lore,
				 StringValue... modifiers) {
		this.func = func;
		this.override = override;
		this.period = period;
		this.lore = lore;

		for (StringValue modifier : modifiers)
			this.modifiers.put(modifier.getName(), modifier.getValue());
	}

	public String getDefaultName() {
		return MMOUtils.caseOnWords(name().toLowerCase().replace("_", " "));
	}

	public double getModifier(String path) {
		return modifiers.get(path);
	}

	public Set<String> getModifiers() {
		return modifiers.keySet();
	}

	public String getDescription() {
		return lore;
	}

	public boolean hasPriority() {
		return override;
	}

	public long getTime() {
		return period;
	}

	public ParticleRunnable newRunnable(ParticleData particle, PlayerData player) {
		return func.apply(particle, player);
	}
}