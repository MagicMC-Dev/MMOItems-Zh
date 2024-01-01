package net.Indyuce.mmoitems.api.item.template;

import io.lumine.mythic.lib.UtilityMethods;
import net.Indyuce.mmoitems.util.MMOUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

public class NameModifier {
	private final ModifierType type;
	private final String format;
	private final int priority;

	/**
	 * Loads a prefix/suffix from either a config section or a string
	 * 
	 * @param type
	 *            Either a prefix or a suffix
	 * @param object
	 *            The object to load the modifier from
	 */
	public NameModifier(ModifierType type, Object object) {
		Validate.notNull(object, "Object cannot be null");
		this.type = type;

		if (object instanceof String) {
			format = (String) object;
			priority = 0;
			return;
		}

		if (object instanceof ConfigurationSection) {
			ConfigurationSection config = (ConfigurationSection) object;
			Validate.isTrue(config.contains("format"), UtilityMethods.caseOnWords(type.name().toLowerCase()) + " format cannot be null");
			format = config.get("format").toString();
			priority = config.getInt("priority");
			return;
		}

		throw new IllegalArgumentException("Must specify a string or a config section");
	}

	public NameModifier(ModifierType type, String format, int priority) {
		Validate.notNull(format, "Format cannot be null");
		this.type = type;
		this.format = format;
		this.priority = priority;

		Validate.notNull(type, "Type cannot be null");
	}

	public String getFormat() {
		return format;
	}

	public ModifierType getType() {
		return type;
	}

	public boolean hasPriority() {
		return priority > 0;
	}

	public int getPriority() {
		return priority;
	}

	public enum ModifierType {
		PREFIX,
		SUFFIX
	}
}
