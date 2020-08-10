package net.Indyuce.mmoitems.api.item.template;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate.TemplateOption;
import net.Indyuce.mmoitems.api.item.template.NameModifier.ModifierType;
import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;

public class MMOItemBuilder {
	private final MMOItem mmoitem;
	private final int level;
	private final ItemTier tier;

	/**
	 * Capacity is not final as it keeps lowering as modifiers are selected and
	 * applied
	 */
	private double capacity;

	/**
	 * Name modifiers, prefixes or suffixes, with priorities. They are saved
	 * because they must be applied after the modifier selection process
	 */
	private final Set<NameModifier> nameModifiers = new HashSet<>();

	/**
	 * Instance which is created everytime an mmoitem is being randomly
	 * generated
	 * 
	 * @param template
	 *            The mmoitem template used to generate an item.
	 * @param level
	 *            Specified item level.
	 * @param tier
	 *            Specified item level which determines how many capacity it
	 *            will have. If no tier is given, item uses the default capacity
	 *            formula given in the main config file
	 */
	public MMOItemBuilder(MMOItemTemplate template, int level, ItemTier tier) {
		this.level = level;
		this.tier = tier;
		this.capacity = (tier != null ? tier.getCapacity() : MMOItems.plugin.getLanguage().defaultItemCapacity).calculate(level);
		this.mmoitem = new MMOItem(template.getType(), template.getId());

		// apply base item data
		template.getBaseItemData().entrySet().forEach(entry -> applyData(entry.getKey(), entry.getValue().randomize(this)));

		if (tier != null)
			mmoitem.setData(ItemStat.TIER, new StringData(tier.getId()));

		// roll item gen modifiers
		for (TemplateModifier modifier : rollModifiers(template)) {

			// roll modifier change
			if (!modifier.rollChance())
				continue;

			// only apply if enough item weight
			if (modifier.getWeight() > capacity)
				continue;

			capacity -= modifier.getWeight();
			if (modifier.hasNameModifier())
				addModifier(modifier.getNameModifier());
			modifier.getItemData().forEach((stat, data) -> applyData(stat, data.randomize(this)));
		}
	}

	public int getLevel() {
		return level;
	}

	public double getRemainingCapacity() {
		return capacity;
	}

	public ItemTier getTier() {
		return tier;
	}

	public MMOItem build() {

		/*
		 * calculate new display name with suffixes and prefixes if display name
		 * cannot be found, MMOItems is used "Item" by default so the user has
		 * to specify a default name
		 */
		if (!nameModifiers.isEmpty()) {
			String displayName = mmoitem.hasData(ItemStat.NAME) ? mmoitem.getData(ItemStat.NAME).toString() : "Item";
			for (NameModifier mod : nameModifiers) {
				if (mod.getType() == ModifierType.PREFIX)
					displayName = mod.getFormat() + " " + displayName;
				if (mod.getType() == ModifierType.SUFFIX)
					displayName += " " + mod.getFormat();
			}

			mmoitem.setData(ItemStat.NAME, new StringData(displayName));
		}

		return mmoitem;
	}

	public void applyData(ItemStat stat, StatData data) {
		if (mmoitem.hasData(stat) && data instanceof Mergeable)
			((Mergeable) mmoitem.getData(stat)).merge(data);
		else
			mmoitem.setData(stat, data);
	}

	public void addModifier(NameModifier modifier) {
		// clean less-priority name modifiers w/ same type only
		nameModifiers.removeIf(current -> current.getType() == modifier.getType() && current.getPriority() < modifier.getPriority());
		nameModifiers.add(modifier);
	}

	private Collection<TemplateModifier> rollModifiers(MMOItemTemplate template) {
		if (!template.hasOption(TemplateOption.ROLL_MODIFIER_CHECK_ORDER))
			return template.getModifiers().values();

		List<TemplateModifier> modifiers = new ArrayList<>(template.getModifiers().values());
		Collections.shuffle(modifiers);
		return modifiers;
	}
}
