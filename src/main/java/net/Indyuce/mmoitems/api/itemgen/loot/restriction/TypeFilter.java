package net.Indyuce.mmoitems.api.itemgen.loot.restriction;

import java.util.function.Predicate;

import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.itemgen.GenerationTemplate;

public class TypeFilter implements Predicate<GenerationTemplate> {
	private final Type type;

	public TypeFilter(Type type) {
		this.type = type;
	}

	@Override
	public boolean test(GenerationTemplate template) {
		return template.getType().equals(type);
	}
}
