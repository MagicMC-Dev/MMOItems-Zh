package net.Indyuce.mmoitems.api.item.template.explorer;

import java.util.function.Predicate;

import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;

/**
 * Filters items with a specific ID
 */
public class IDFilter implements Predicate<MMOItemTemplate> {
	private final String id;

	public IDFilter(String id) {
		this.id = id;
	}

	@Override
	public boolean test(MMOItemTemplate template) {
		return template.getId().equalsIgnoreCase(id);
	}
}
