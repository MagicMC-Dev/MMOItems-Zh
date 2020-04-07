package net.Indyuce.mmoitems.api.itemgen;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import net.Indyuce.mmoitems.MMOUtils;

public class NameModifier {
	private final ModifierType type;
	private final String format;
	private final int priority;

	public NameModifier(ConfigurationSection config) {
		Validate.notNull(config, "Prefix/suffix config cannot be null");
		type = ModifierType.valueOf(config.getName().toUpperCase());

		Validate.isTrue(config.contains("format"), MMOUtils.caseOnWords(type.name().toLowerCase()) + " format cannot be null");
		format = ChatColor.translateAlternateColorCodes('&', config.get("format").toString());

		priority = config.getInt("priority");
	}

	public NameModifier(ModifierType type, String format, int priority) {
		Validate.notNull(format, "Format cannot be null");
		this.type = type;
		this.format = ChatColor.translateAlternateColorCodes('&', format);
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

	public static enum ModifierType {
		PREFIX,
		SUFFIX;
	}
}
