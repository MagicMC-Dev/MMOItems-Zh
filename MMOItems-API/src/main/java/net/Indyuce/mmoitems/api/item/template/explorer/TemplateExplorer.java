package net.Indyuce.mmoitems.api.item.template.explorer;

import java.util.Collection;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.player.RPGPlayer;

/**
 * Used to explore currently registered templates and randomly pick a template
 * given specific restrictions like type/class restrictions. Warning, this
 * caches in a collection all currently registered templates and applies filters
 * so this can be performance heavy
 * 
 * @author cympe
 */
public class TemplateExplorer {
	private final Random random = new Random();

	/*
	 * Not defined at the beginning to save extra performance,
	 * if there are 100+ templates this be pretty violent
	 */
	private final Collection<MMOItemTemplate> all = MMOItems.plugin.getTemplates().collectTemplates();

	public int count() {
		return all.size();
	}

	public TemplateExplorer applyFilter(Predicate<MMOItemTemplate> filter) {
		all.removeIf(not(filter));
		return this;
	}

	public Optional<MMOItemTemplate> rollLoot() {
		switch (count()) {
		case 0:
			return Optional.empty();
		case 1:
			return all.stream().findFirst();
		default:
			return all.stream().skip(random.nextInt(count())).findFirst();
		}
	}

	/**
	 * Util method to easily generate random MI loot
	 * 
	 * @param player
	 *            The player
	 * @return Random item with random tier and item level which matches the
	 *         player's level
	 */
	public Optional<MMOItem> rollItem(RPGPlayer player) {
		return rollLoot().map(template -> template.newBuilder(player).build());
	}

	private <T> Predicate<T> not(Predicate<T> predicate) {
		return t -> !predicate.test(t);
	}
}
