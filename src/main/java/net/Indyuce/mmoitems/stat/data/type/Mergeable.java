package net.Indyuce.mmoitems.stat.data.type;

import org.jetbrains.annotations.NotNull;

public interface Mergeable {

	/**
	 * Merging two stat data is used when either applying a gem stone to an item
	 * which already has this type of item data, or when generating an item
	 * randomly so that the item benefits from all modifiers
	 */
	void merge(StatData data);

	/**
	 * Returns a Data with the same values as this, but that is not this.
	 */
	@NotNull StatData cloneData();
}
