package net.Indyuce.mmoitems.api.util;

import org.bukkit.Material;

public class MushroomState {
	public final Material id;
	public final boolean up, down, west, east, south, north;

	/*
	 * help generate an unique identifier that only depends on the parameters of
	 * a mushroom block. ids are unique because they correspond to one specific
	 * mushroom state
	 */
	public MushroomState(Material id, boolean up, boolean down, boolean west, boolean east, boolean south, boolean north) {
		this.id = id;
		this.up = up;
		this.down = down;
		this.west = west;
		this.east = east;
		this.south = south;
		this.north = north;
	}

	public int getUniqueId() {
		String first = id == Material.BROWN_MUSHROOM_BLOCK ? "0" : id == Material.RED_MUSHROOM_BLOCK ? "1" : "2";
		return Integer.parseInt(first + (up ? "1" : "0") + (down ? "1" : "0") + (west ? "1" : "0") + (east ? "1" : "0") + (south ? "1" : "0") + (north ? "1" : "0"));
	}

	public boolean equals(MushroomState state) {
		return up == state.up && down == state.down && west == state.west && east == state.east && south == state.south && north == state.north;
	}
}
