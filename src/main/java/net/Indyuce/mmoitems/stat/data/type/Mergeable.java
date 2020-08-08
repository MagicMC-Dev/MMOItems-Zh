package net.Indyuce.mmoitems.stat.data.type;

public interface Mergeable {

	/**
	 * Merging two stat data is used when either applying a gem stone to an item
	 * which already has this type of item data, or when generating an item
	 * randomly so that the item benefits from all modifiers
	 */
	public void merge(StatData data);
}
