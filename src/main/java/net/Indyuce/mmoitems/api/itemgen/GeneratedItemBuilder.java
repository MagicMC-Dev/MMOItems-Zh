package net.Indyuce.mmoitems.api.itemgen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.itemgen.NameModifier.ModifierType;
import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;

public class GeneratedItemBuilder {
	private final int level;
	private final MMOItem mmoitem;

	/*
	 * weight is not final because it is lowered as modifiers are applied to the
	 * MMOItem.
	 */
	private double weight;

	/*
	 * name modifiers which must be applied at the end of the item generation
	 * process
	 */
	private final Set<NameModifier> nameModifiers = new HashSet<>();

	private static final Random random = new Random();

	/*
	 * instance is created everytime an item is being randomly generated.
	 */
	public GeneratedItemBuilder(GenerationTemplate template, int level, double spread) {
		this.level = rollLevel(level, spread);
		weight = template.calculateWeight(level);
		mmoitem = new MMOItem(template.getType(), template.getId());

		// apply base item data
		template.getBaseItemData().entrySet().forEach(entry -> applyData(entry.getKey(), entry.getValue().randomize(this)));

		// roll item gen modifiers
		List<GenerationModifier> lookUpOrder = new ArrayList<>(template.getModifiers());
		Collections.shuffle(lookUpOrder);
		for (GenerationModifier modifier : lookUpOrder) {

			// roll modifier change
			if (!modifier.rollChance())
				continue;

			// only apply if enough item weight
			if (modifier.getWeight() > weight)
				continue;

			weight -= modifier.getWeight();
			if (modifier.hasNameModifier())
				addModifier(modifier.getNameModifier());
			modifier.getItemData().forEach((stat, data) -> applyData(stat, data.randomize(this)));
		}

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
	}

	public int getLevel() {
		return level;
	}

	public double getWeight() {
		return weight;
	}

	public MMOItem build() {
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

	/*
	 * formula to generate the item level. input is the player level and the
	 * level spread which corresponds to the standard deviation of a gaussian
	 * distribution centered on the player level
	 */
	private int rollLevel(int level, double s) {
		double found = random.nextGaussian() * s + level;

		// cannot be more than 2x the level and must be higher than 1
		found = Math.max(Math.min(2 * level, found), 1);

		return (int) found;
	}
}
