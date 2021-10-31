package net.Indyuce.mmoitems.ability;

import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.player.cooldown.CooldownObject;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class Ability<T extends AbilityMetadata> implements CooldownObject {
	private final String name, id;
	private final Map<String, Double> modifiers = new HashMap<>();

	protected static final Random random = new Random();

	public Ability() {
		this.id = getClass().getSimpleName().toUpperCase().replace("-", "_").replace(" ", "_").replaceAll("[^A-Z_]", "");
		this.name = getClass().getSimpleName().replace("_", " ");
	}

	public Ability(String id, String name) {
		Validate.notNull(id, "Id cannot be null");
		Validate.notNull(name, "Name cannot be null");

		this.id = id.toUpperCase().replace("-", "_").replace(" ", "_").replaceAll("[^A-Z_]", "");
		this.name = name;
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

	public double getDefaultValue(String path) {
		return modifiers.get(path);
	}

	public Set<String> getModifiers() {
		return modifiers.keySet();
	}

	public void addModifier(String modifier, double defaultValue) {
		modifiers.put(modifier, defaultValue);
	}

	@Override
	public String getCooldownPath() {
		return "mmoitems_skill_" + id.toLowerCase();
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
	public abstract T canBeCast(AttackMetadata attack, LivingEntity target, AbilityData ability);

	/**
	 * Called when a player successfully casts an ability
	 *
	 * @param attack  Information concerning the current player attack as well
	 *                as his MythicLib cached stat map
	 * @param ability All the information about the ability being cast. This is the
	 *                same instance as the one returned in whenRan(..)
	 */
	public abstract void whenCast(AttackMetadata attack, T ability);

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Ability)) {
			return false;
		}

		// Same name means same ability
		return ((Ability) obj).getName().equals(getName());
	}
}
