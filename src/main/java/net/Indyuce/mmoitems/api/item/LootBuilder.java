package net.Indyuce.mmoitems.api.item;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.player.RPGPlayer;

public class LootBuilder {

	/*
	 * not defined at the beginning to save extra performance, if there are 100+
	 * templates this be pretty violent. TODO make type reference necessary?
	 */
	private final Collection<MMOItemTemplate> all;

	/*
	 * options required to generate random loot
	 */
	private final ItemTier itemTier;
	private final int itemLevel;

	public LootBuilder(RPGPlayer player, ItemTier itemTier) {
		itemLevel = MMOItems.plugin.getTemplates().rollLevel(player.getLevel());
		this.itemTier = itemTier;
		all = MMOItems.plugin.getTemplates().collectTemplates();
	}

	public LootBuilder(RPGPlayer player) {
		itemLevel = MMOItems.plugin.getTemplates().rollLevel(player.getLevel());
		itemTier = MMOItems.plugin.getTemplates().rollTier();
		all = MMOItems.plugin.getTemplates().collectTemplates();
	}

	public LootBuilder(int itemLevel, ItemTier itemTier) {
		this.itemLevel = itemLevel;
		this.itemTier = itemTier;
		all = MMOItems.plugin.getTemplates().collectTemplates();
	}

	public int count() {
		return all.size();
	}

	public LootBuilder applyFilter(Predicate<MMOItemTemplate> filter) {
		all.removeIf(not(filter));
		return this;
	}

	public MMOItem rollLoot() {
		Optional<MMOItemTemplate> found = all.stream().findAny();
		return found.isPresent() ? found.get().newBuilder(itemLevel, itemTier).build() : null;
	}

	private <T> Predicate<T> not(Predicate<T> predicate) {
		return t -> !predicate.test(t);
	}
}
