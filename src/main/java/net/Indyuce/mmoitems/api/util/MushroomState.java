package net.Indyuce.mmoitems.api.util;

import org.bukkit.Material;

public class MushroomState {
	public Material id;
	public boolean up, down, west, east, south, north;
	
	public MushroomState(Material id, boolean up, boolean down, boolean west, boolean east, boolean south, boolean north) {
		this.id = id;
		this.up = up; this.down = down; this.west = west;
		this.east = east; this.south = south; this.north = north;
	}
	
	public boolean equals(MushroomState state) {
		return up == state.up && down == state.down && west == state.west &&
			east == state.east && south == state.south && north == state.north;
	}
	
	public int getUniqueId() {
		String i;
		if(id == Material.BROWN_MUSHROOM_BLOCK) i = "0";
		else if(id == Material.RED_MUSHROOM_BLOCK) i = "1";
		else i = "2";
		
		return Integer.parseInt(i + (up ? "1" : "0") + (down ? "1" : "0") + (west ? "1" : "0") +
			(east ? "1" : "0") + (south ? "1" : "0") + (north ? "1" : "0"));
	}
}
