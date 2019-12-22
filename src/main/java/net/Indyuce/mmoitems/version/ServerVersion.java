package net.Indyuce.mmoitems.version;

import net.Indyuce.mmoitems.version.wrapper.VersionWrapper;
import net.Indyuce.mmoitems.version.wrapper.VersionWrapper_1_13;
import net.Indyuce.mmoitems.version.wrapper.VersionWrapper_1_14;
import net.Indyuce.mmoitems.version.wrapper.VersionWrapper_Legacy;

public class ServerVersion {
	private final String version;
	private final int[] integers;

	private final VersionWrapper versionWrapper;

	public ServerVersion(Class<?> clazz) {
		version = clazz.getPackage().getName().replace(".", ",").split(",")[3];
		String[] split = version.substring(1).split("\\_");
		integers = new int[] { Integer.parseInt(split[0]), Integer.parseInt(split[1]) };

		versionWrapper = isBelowOrEqual(1, 12) ? new VersionWrapper_Legacy() : isBelowOrEqual(1, 13) ? new VersionWrapper_1_13() : new VersionWrapper_1_14();
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

	public VersionWrapper getWrapper() {
		return versionWrapper;
	}

	@Override
	public String toString() {
		return version;
	}
}
