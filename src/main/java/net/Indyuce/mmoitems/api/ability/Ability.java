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

	/**
	 * @return The list of all the casting modes which are compatible with this
	 *         ability
	 */
	public List<CastingMode> getSupportedCastingModes() {
		return allowedModes;
	}

	public double getDefaultValue(String path) {
		return modifiers.get(path);
	}

	public Set<String> getModifiers() {
		return modifiers.keySet();
	}

	public void addModifier(String modifier, double defaultValue) {
		modifiers.put(modifier, defaultValue);
	}

	/**
	 * Disables an ability. This method must be called before MMOItems registers
	 * this ability since it is just a boolean check when registering abilities
	 * through the abilityManager
	 */
	public void disable() {
		enabled = false;
	}

	/**
	 * The first method called when a player uses an ability.
	 * 
	 * @param stats
	 *            Cached statistics of the player casting the ability
	 * @param target
	 *            The eventual ability target
	 * @param ability
	 *            The ability being cast
	 * @param result
	 *            The melee attack result, which can be edited to
	 *            increase/decrease final damage dealt
	 * @return If the ability can be cast or not. AbilityResult should cache any
	 *         information used by the ability: target if found, etc.
	 */
	public abstract AbilityResult whenRan(CachedStats stats, LivingEntity target, AbilityData ability, ItemAttackResult result);

	/**
	 * Called when a player successfully casts an ability
	 * 
	 * @param stats
	 *            Cached statistics of the player casting the ability
	 * @param ability
	 *            All the information about the ability being cast. This is the
	 *            same instance as the one returned in whenRan(..)
	 * @param result
	 *            The melee attack result, which can be edited to
	 *            increase/decrease final damage dealt
	 */
	public abstract void whenCast(CachedStats stats, AbilityResult ability, ItemAttackResult result);

	public enum CastingMode {

		/**
		 * When the player hits another entity
		 */
		ON_HIT(false),

		/**
		 * When the player is hit by another entity
		 */
		WHEN_HIT(false),

		/**
		 * When the player performs a left click
		 */
		LEFT_CLICK,

		/**
		 * When the player performs a right click
		 */
		RIGHT_CLICK,

		/**
		 * Performing a left click while sneaking
		 */
		SHIFT_LEFT_CLICK,

		/**
		 * Performing a right click while sneaking
		 */
		SHIFT_RIGHT_CLICK;

		private final boolean message;

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

		public String getLowerCaseId() {
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
