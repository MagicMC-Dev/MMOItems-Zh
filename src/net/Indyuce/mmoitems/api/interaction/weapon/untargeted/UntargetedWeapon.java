package net.Indyuce.mmoitems.api.interaction.weapon.untargeted;

import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.EquipmentSlot;

import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.interaction.util.DurabilityItem;
import net.Indyuce.mmoitems.api.interaction.util.InteractItem;
import net.Indyuce.mmoitems.api.interaction.weapon.Weapon;
import net.Indyuce.mmoitems.api.item.NBTItem;

public abstract class UntargetedWeapon extends Weapon {

	/*
	 * this final field is used in the AttackResult constructor to be able to
	 * cast on-hit abilities since the weapon is untargeted.
	 */
	protected final UntargetedWeapon untargeted = this;
	protected final WeaponType weaponType;

	public UntargetedWeapon(Player player, NBTItem item, Type type, WeaponType weaponType) {
		super(player, item, type);

		this.weaponType = weaponType;
	}

	/*
	 * called first when the player clicks his item and allows to apply
	 * durability onto a weapon that is not targeted
	 */
	public final void untargetedAttack(EquipmentSlot slot) {
		DurabilityItem durItem = new DurabilityItem(getPlayer(), getNBTItem());
		if (durItem.isValid())
			new InteractItem(getPlayer(), slot).setItem(durItem.decreaseDurability(1).toItem());

		untargetedAttackEffects();
	}

	public WeaponType getWeaponType() {
		return weaponType;
	}

	public abstract void untargetedAttackEffects();

	public enum WeaponType {
		RIGHT_CLICK,
		LEFT_CLICK;

		public boolean corresponds(Action action) {
			return (this == RIGHT_CLICK && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) || (this == LEFT_CLICK && (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK));
		}
	}
}
