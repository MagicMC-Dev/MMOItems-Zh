package net.Indyuce.mmoitems.api.interaction.weapon.untargeted;

import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.EquipmentSlot;

import net.Indyuce.mmoitems.api.interaction.weapon.Weapon;
import net.mmogroup.mmolib.api.item.NBTItem;

public abstract class UntargetedWeapon extends Weapon {
	protected final WeaponType weaponType;

	public UntargetedWeapon(Player player, NBTItem item, WeaponType weaponType) {
		super(player, item);

		this.weaponType = weaponType;
	}

	/*
	 * called first when the player clicks his item and allows to apply
	 * durability onto a weapon that is not targeted
	 */
	public abstract void untargetedAttack(EquipmentSlot slot);

	public WeaponType getWeaponType() {
		return weaponType;
	}

	public enum WeaponType {
		RIGHT_CLICK,
		LEFT_CLICK;

		public boolean corresponds(Action action) {
			return (this == RIGHT_CLICK && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK))
					|| (this == LEFT_CLICK && (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK));
		}
	}
}
