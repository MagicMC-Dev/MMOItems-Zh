package net.Indyuce.mmoitems.api.util;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.MultipleFacing;

public class MushroomState {
	private final Material material;
	private final boolean up, down, west, east, south, north;

	/*
	 * help generate an unique identifier that only depends on the parameters of
	 * a mushroom block. ids are unique because they correspond to one specific
	 * mushroom state
	 */
	public MushroomState(Material material, boolean up, boolean down, boolean west, boolean east, boolean south, boolean north) {
		this.material = material;
		this.up = up;
		this.down = down;
		this.west = west;
		this.east = east;
		this.south = south;
		this.north = north;
	}

	public int getUniqueId() {
		String first = material == Material.BROWN_MUSHROOM_BLOCK ? "0" : material == Material.RED_MUSHROOM_BLOCK ? "1" : "2";
		return Integer.parseInt(
				first + (up ? "1" : "0") + (down ? "1" : "0") + (west ? "1" : "0") + (east ? "1" : "0") + (south ? "1" : "0") + (north ? "1" : "0"));
	}

	public boolean matches(MushroomState state) {
		return up == state.up && down == state.down && west == state.west && east == state.east && south == state.south && north == state.north;
	}

	public Material getType() {
		return material;
	}

	// From the Id, check which sides to apply data to.
	// This will return the blockstate of the blocks id.
	public BlockData getBlockData() {
		MultipleFacing blockData = (MultipleFacing) material.createBlockData();

		blockData.setFace(BlockFace.UP, up);
		blockData.setFace(BlockFace.DOWN, down);
		blockData.setFace(BlockFace.NORTH, north);
		blockData.setFace(BlockFace.SOUTH, south);
		blockData.setFace(BlockFace.EAST, east);
		blockData.setFace(BlockFace.WEST, west);

		return blockData;
	}
	
	public boolean getSide(String side) {
		side = side.toLowerCase();
		switch(side) {
			case "up":
				return up;
			case "down":
				return down;
			case "north":
				return north;
			case "south":
				return south;
			case "east":
				return east;
			case "west":
				return west;
			default:
				return false;
		}
	}
}
