package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.stat.type.DoubleStat;
import org.bukkit.Material;

public class MovementSpeed extends DoubleStat {
	public MovementSpeed() {
		super("MOVEMENT_SPEED", Material.LEATHER_BOOTS, "移动速度", new String[] { "移动速度增加步行速度, ", "(默认MC行走速度: 0.1)" });
	}

	@Override
	public double multiplyWhenDisplaying() { return 100; }
}
