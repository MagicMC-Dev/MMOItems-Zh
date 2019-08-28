package net.Indyuce.mmoitems.stat.type;

import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.stat.data.upgrade.UpgradeInfo;

public interface Upgradable {

	/*
	 * argument 'obj' is either a primitive object like string, boolean or
	 * double or a configuration section. the method must check if this argument
	 * is of the right type. this method ONLY handles IllegalArgumentExceptions
	 */
	public UpgradeInfo loadUpgradeInfo(Object obj);

	public void apply(MMOItem mmoitem, UpgradeInfo info);
}
