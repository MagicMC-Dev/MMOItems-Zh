package net.Indyuce.mmoitems.api.itemgen.loot;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.itemgen.GenerationTemplate;
import net.Indyuce.mmoitems.api.itemgen.tier.RolledTier;
import net.Indyuce.mmoitems.api.player.RPGPlayer;

public class LootBuilder {

	/*
	 * not defined at the beginning to save extra performance, if there are 100+
	 * templates this be pretty violent. TODO make type reference necessary?
	 */
	private final Collection<GenerationTemplate> all;

	/*
	 * options required to generate random loot
	 */
	private final RolledTier itemTier;
	private final int itemLevel;

	public LootBuilder(RPGPlayer player, ItemTier itemTier) {
		itemLevel = MMOItems.plugin.getItemGenerator().rollLevel(player.getLevel());
		this.itemTier = MMOItems.plugin.getItemGenerator().getTierInfo(itemTier).roll(itemLevel);
		all = MMOItems.plugin.getItemGenerator().getTemplates();
	}

	public LootBuilder(RPGPlayer player) {
		itemLevel = MMOItems.plugin.getItemGenerator().rollLevel(player.getLevel());
		itemTier = MMOItems.plugin.getItemGenerator().rollTier(itemLevel);
		all = MMOItems.plugin.getItemGenerator().getTemplates();
	}

	public LootBuilder(int itemLevel, RolledTier itemTier) {
		this.itemLevel = itemLevel;
		this.itemTier = itemTier;
		all = MMOItems.plugin.getItemGenerator().getTemplates();
	}

	public int count() {
		return all.size();
	}

	public LootBuilder applyFilter(Predicate<GenerationTemplate> filter) {
		all.removeIf(not(filter));
		return this;
	}

	public MMOItem rollLoot() {
		Optional<GenerationTemplate> found = all.stream().findAny();
		return found.isPresent() ? found.get().newBuilder(itemLevel, itemTier).build() : null;
	}

	private <T> Predicate<T> not(Predicate<T> predicate) {
		return t -> !predicate.test(t);
	}
}
