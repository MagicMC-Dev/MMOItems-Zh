package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.stat.type.DoubleStat;
import org.bukkit.Material;

public class MovementSpeed extends DoubleStat {
	public MovementSpeed() {
		super("MOVEMENT_SPEED", Material.LEATHER_BOOTS, "Movement Speed", new String[] { "Movement Speed increase walk speed.", "Default MC walk speed: 0.1" });
	}

	@Override
	public double multiplyWhenDisplaying() { return 100; }
}
