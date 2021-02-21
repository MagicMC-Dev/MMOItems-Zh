package net.Indyuce.mmoitems.stat.data.type;

import net.Indyuce.mmoitems.stat.type.StatHistory;
import net.Indyuce.mmoitems.stat.type.Upgradable;
import org.jetbrains.annotations.NotNull;

/**
 * Most intuitive use is for ItemStats to not completely replace each other
 * when used through Gem Stones. However, this serves a crucial internal
 * role in determining which stats generate {@link StatHistory}es, which in
 * turn allows them to be {@link Upgradable}.
 */
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
