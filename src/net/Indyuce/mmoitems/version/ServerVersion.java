package net.Indyuce.mmoitems.version;

import net.Indyuce.mmoitems.version.durability.CustomModelDataHandler;
import net.Indyuce.mmoitems.version.durability.DurabilityHandler;
import net.Indyuce.mmoitems.version.durability.Durability_v1_13_Handler;
import net.Indyuce.mmoitems.version.durability.LegacyDurabilityHandler;
import net.Indyuce.mmoitems.version.wrapper.DefaultVersionWrapper;
import net.Indyuce.mmoitems.version.wrapper.LegacyVersionWrapper;
import net.Indyuce.mmoitems.version.wrapper.VersionWrapper;

public class ServerVersion {
	private final String version;
	private final int[] integers;

	private final DurabilityHandler durabilityHandler;
	private final VersionWrapper versionWrapper;

	public ServerVersion(Class<?> clazz) {
		version = clazz.getPackage().getName().replace(".", ",").split(",")[3];
		String[] split = version.substring(1).split("\\_");
		integers = new int[] { Integer.parseInt(split[0]), Integer.parseInt(split[1]) };

		versionWrapper = isBelowOrEqual(1, 12) ? new LegacyVersionWrapper() : new DefaultVersionWrapper();
		durabilityHandler = isBelowOrEqual(1, 12) ? new LegacyDurabilityHandler() : isBelowOrEqual(1, 13) ? new Durability_v1_13_Handler() : new CustomModelDataHandler();
	}

	public boolean isBelowOrEqual(int... version) {
		return version[0] > integers[0] ? true : version[1] >= integers[1];
	}

	public boolean isStrictlyHigher(int... version) {
		return version[0] < integers[0] ? true : version[1] < integers[1];
		// return !isBelowOrEqual(version);
	}

	public int getRevisionNumber() {
		return Integer.parseInt(version.split("\\_")[2].replaceAll("[^0-9]", ""));
	}

	public int[] toNumbers() {
		return integers;
	}

	public DurabilityHandler getDurabilityHandler() {
		return durabilityHandler;
	}

	public VersionWrapper getVersionWrapper() {
		return versionWrapper;
	}


	@Override
	public String toString() {
		return version;
	}
}
