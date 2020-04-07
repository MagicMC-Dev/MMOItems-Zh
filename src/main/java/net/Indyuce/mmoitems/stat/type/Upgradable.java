package net.Indyuce.mmoitems.stat.type;

import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.stat.data.type.UpgradeInfo;

public interface Upgradable {

	/*
	 * an upgradable stat can be used in an upgrade template to be upgraded!
	 * TODO add abilities so that ability damage, effect duration etc. can
	 * increase when upgrading the item!
	 */

	/*
	 * argument 'obj' is either a primitive object like string, boolean or
	 * double or a configuration section. the method must check if this argument
	 * is of the right type. this method ONLY handles IllegalArgumentExceptions
	 */
	public UpgradeInfo loadUpgradeInfo(Object obj);

	public void apply(MMOItem mmoitem, UpgradeInfo info);
}
