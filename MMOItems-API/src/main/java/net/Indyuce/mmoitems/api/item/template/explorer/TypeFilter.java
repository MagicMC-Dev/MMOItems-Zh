package net.Indyuce.mmoitems.api.item.template.explorer;

import java.util.function.Predicate;

import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;

/**
 * Filters items with a specific type
 */
public class TypeFilter implements Predicate<MMOItemTemplate> {
	private final Type type;

	public TypeFilter(Type type) {
		this.type = type;
	}

	@Override
	public boolean test(MMOItemTemplate template) {
		return template.getType().equals(type);
	}
}
