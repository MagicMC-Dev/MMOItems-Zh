package net.Indyuce.mmoitems.api.itemgen;

import java.util.HashSet;
import java.util.Set;

import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.itemgen.NameModifier.ModifierType;
import net.Indyuce.mmoitems.api.itemgen.tier.RolledTier;
import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;

public class GeneratedItemBuilder {
	private final int level;
	private final MMOItem mmoitem;
	private final RolledTier tier;

	/*
	 * capacity is not final because it is lowered as modifiers are applied
	 */
	private double capacity;

	/*
	 * name modifiers which must be applied at the end of the item generation
	 * process
	 */
	private final Set<NameModifier> nameModifiers = new HashSet<>();

	/*
	 * instance is created everytime an item is being randomly generated.
	 */
	public GeneratedItemBuilder(GenerationTemplate template, int level, RolledTier tier) {
		this.level = level;
		this.tier = tier;
		this.capacity = tier.getCapacity();
		this.mmoitem = new MMOItem(template.getType(), template.getId());

		// apply base item data
		template.getBaseItemData().entrySet().forEach(entry -> applyData(entry.getKey(), entry.getValue().randomize(this)));

		if (!tier.isDefault())
			mmoitem.setData(ItemStat.TIER, new StringData(tier.getTier().getId()));

		// roll item gen modifiers
		for (GenerationModifier modifier : template.getModifiers()) {

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

	public RolledTier getTier() {
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
}
