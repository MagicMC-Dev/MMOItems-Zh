package net.Indyuce.mmoitems.api.interaction.weapon.untargeted;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

import net.Indyuce.mmoitems.api.interaction.weapon.Weapon;
import io.lumine.mythic.lib.api.item.NBTItem;

public abstract class UntargetedWeapon extends Weapon {
	protected final WeaponType weaponType;

	public UntargetedWeapon(Player player, NBTItem item, WeaponType weaponType) {
		super(player, item);

		this.weaponType = weaponType;
	}

	/**
	 * Called when the player interacts with the item. This method is used to
	 * apply durability and cast the weapon attack
	 * 
	 * @param slot Slot being interacted with
	 */
	public abstract void untargetedAttack(EquipmentSlot slot);

	public WeaponType getWeaponType() {
		return weaponType;
	}

	/**
	 * Used to determine if the item must be left or right clicked in order to
	 * cast a basic attack. Whips, staffs are left click weapons whereas muskets
	 * are right click weapons
	 * 
	 * @author cympe
	 *
	 */
	public static enum WeaponType {
		RIGHT_CLICK,
		LEFT_CLICK;

		public boolean corresponds(Action action) {
			return (this == RIGHT_CLICK && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK))
					|| (this == LEFT_CLICK && (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK));
		}
	}
}
