package net.Indyuce.mmoitems.api.item;

import net.Indyuce.mmoitems.api.Type;

/**
 * Referenced objects are either item templates or MMOItems. They contain the
 * reference for an item template and can be used as inputs in many methods to
 * find some data
 * 
 * @author cympe
 */
public interface ItemReference {

	/**
	 * @return The item type
	 */
	Type getType();

	/**
	 * @return The item ID
	 */
	String getId();
}
