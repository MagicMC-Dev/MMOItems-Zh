package net.Indyuce.mmoitems.stat.type;

import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.data.type.UpgradeInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *  The methods required for this ItemStat to be Upgradeable. <p></p>
 *  <b>It makes sense then that the <code>StatData</code> this uses
 *  implements {@link Mergeable}</b> and it is even assumed so.
 */
public interface Upgradable {

	/*
	 * an upgradable stat can be used in an upgrade template to be upgraded!
	 * TODO add abilities so that ability damage, effect duration etc. can
	 * increase when upgrading the item!
	 */

	/**
	 * When an {@link net.Indyuce.mmoitems.api.UpgradeTemplate} is read from a YML file,
	 * it loads each stat's {@link UpgradeInfo} through this method, passing on the
	 * object mapped to the stat in the configuration.
	 * <p></p>
	 * For example:
	 * <p><code>attack-damage: <b>10%</b></code>
	 * </p>Passes that <code><b>10%</b></code> as an object (not even as a string yet)
	 * <p></p>
	 * This method ONLY handles <code>IllegalArgumentException</code>s which
	 * you are free to throw.
	 * @param obj Is the thing itself written onto the YML file.
	 *            <p></p>
	 *            It is up to you to know what this is, most universally
	 *            a string that you can get with <code>obj.toString()</code>.
	 *            <p></p>
	 *            If your stat is more complicated than just a number, it
	 *            may be a {@link org.bukkit.configuration.ConfigurationSection}, who knows?
	 * @throws IllegalArgumentException If something (anything) goes wrong.
	 */
	@NotNull UpgradeInfo loadUpgradeInfo(@Nullable Object obj) throws IllegalArgumentException;

	/**
	 * Applies this upgrade info <b>level</b> times to this stat data.
	 * @param level The level of the returned StatData, how many times the Upgrade Info will be applied.
	 *              May be negative if the stat supports it.
	 */
	@NotNull StatData apply(@NotNull StatData original, @NotNull UpgradeInfo info, int level);
}
