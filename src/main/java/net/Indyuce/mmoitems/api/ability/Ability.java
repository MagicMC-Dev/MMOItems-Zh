package net.Indyuce.mmoitems.api.ability;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.bukkit.entity.LivingEntity;

import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;

public abstract class Ability {
	private final String name, id;
	private final List<CastingMode> allowedModes;
	private final Map<String, Double> modifiers = new HashMap<>();

	private boolean enabled = true;

	protected static final Random random = new Random();

	public Ability(CastingMode... allowedModes) {
		this.id = getClass().getSimpleName().toUpperCase().replace("-", "_").replace(" ", "_").replaceAll("[^A-Z_]", "");
		this.name = getClass().getSimpleName().replace("_", " ");
		this.allowedModes = Arrays.asList(allowedModes);
	}

	public Ability(String id, String name, CastingMode... allowedModes) {
		Validate.notNull(id, "Id cannot be null");
		Validate.notNull(name, "Name cannot be null");

		this.id = id.toUpperCase().replace("-", "_").replace(" ", "_").replaceAll("[^A-Z_]", "");
		this.name = name;
		this.allowedModes = Arrays.asList(allowedModes);
	}

	public void addModifier(String modifierPath, double defaultValue) {
		modifiers.put(modifierPath, defaultValue);
	}

	public String getID() {
		return id;
	}

	public String getLowerCaseID() {
		return id.toLowerCase().replace("_", "-");
	}

	public String getName() {
		return name;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean isAllowedMode(CastingMode castingMode) {
		return allowedModes.contains(castingMode);
	}

	public List<CastingMode> getSupportedCastingModes() {
		return allowedModes;
	}

	public double getDefaultValue(String path) {
		return modifiers.get(path);
	}

	public Set<String> getModifiers() {
		return modifiers.keySet();
	}

	/*
	 * when that boolean is set to false, the ability will not register when the
	 * plugin enables which prevents it from being registered in the ability
	 * manager from MMOItems
	 */
	public void disable() {
		enabled = false;
	}

	/*
	 * these methods need to be overriden by ability classes depending on their
	 * ability type
	 */
	public abstract AbilityResult whenRan(CachedStats stats, LivingEntity target, AbilityData ability, ItemAttackResult result);

	public abstract void whenCast(CachedStats stats, AbilityResult ability, ItemAttackResult result);

	public enum CastingMode {

		/*
		 * when the player hits another entity.
		 */
		ON_HIT(false),

		/*
		 * when the player is hit by another entity
		 */
		WHEN_HIT(false),

		/*
		 * when the player performs a simple click
		 */
		LEFT_CLICK,
		RIGHT_CLICK,

		/*
		 * when the player performs a simple click while sneaking
		 */
		SHIFT_LEFT_CLICK,
		SHIFT_RIGHT_CLICK;

		private boolean message;

		private CastingMode() {
			this(true);
		}

		private CastingMode(boolean message) {
			this.message = message;
		}

		public boolean displaysMessage() {
			return message;
		}

		public String getName() {
			return MMOUtils.caseOnWords(name().toLowerCase().replace("_", " "));
		}

		public String getLowerCaseID() {
			return name().toLowerCase().replace("_", "-");
		}

		public static CastingMode safeValueOf(String path) {
			try {
				return CastingMode.valueOf(path.toUpperCase().replace("-", "_").replace(" ", "_").replaceAll("[^A-Z_]", ""));
			} catch (Exception e) {
				return null;
			}
		}
	}
}
