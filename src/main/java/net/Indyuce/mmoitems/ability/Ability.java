package net.Indyuce.mmoitems.ability;

import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class Ability<T extends AbilityMetadata> {
	private final String name, id;
	private final List<CastingMode> allowedModes;
	private final Map<String, Double> modifiers = new HashMap<>();

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
	 *
	 * @deprecated Useless
	 */
	@Deprecated
	public void disable() {
	}

	/**
	 * The first method called when a player uses an ability.
	 *
	 * @param attack  Information concerning the current player attack as well
	 *                as his MythicLib cached stat map
	 * @param target  The eventual ability target
	 * @param ability The ability being cast
	 * @return If the ability can be cast or not. AbilityResult should cache any
	 *         information used by the ability: target if found, etc.
	 *         <p>
	 *         This can also return a null instance which means the ability was not cast.
	 */
	@Nullable
	public abstract T canBeCast(ItemAttackMetadata attack, LivingEntity target, AbilityData ability);

	/**
	 * Called when a player successfully casts an ability
	 *
	 * @param attack  Information concerning the current player attack as well
	 *                as his MythicLib cached stat map
	 * @param ability All the information about the ability being cast. This is the
	 *                same instance as the one returned in whenRan(..)
	 */
	public abstract void whenCast(ItemAttackMetadata attack, T ability);

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

		CastingMode() {
			this(true);
		}

		CastingMode(boolean message) {
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

		public static CastingMode safeValueOf(String format) {
			for (CastingMode mode : values())
				if (mode.name().equals(format))
					return mode;
			return null;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Ability)) {
			return false;
		}

		// Same name means same ability
		return ((Ability) obj).getName().equals(getName());
	}
}
