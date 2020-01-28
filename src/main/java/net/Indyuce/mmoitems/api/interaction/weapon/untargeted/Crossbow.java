package net.Indyuce.mmoitems.api.interaction.weapon.untargeted;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.interaction.util.UntargetedDurabilityItem;
import net.Indyuce.mmoitems.api.player.PlayerData.CooldownType;
import net.Indyuce.mmoitems.api.player.PlayerStats;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.api.item.NBTItem;

public class Crossbow extends UntargetedWeapon {
	public Crossbow(Player player, NBTItem item, Type type) {
		super(player, item, type, WeaponType.RIGHT_CLICK);
	}

	@Override
	public void untargetedAttack(EquipmentSlot slot) {

		// check for arrow
		if (getPlayer().getGameMode() != GameMode.CREATIVE && !getPlayer().getInventory().containsAtLeast(new ItemStack(Material.ARROW), 1))
			return;

		PlayerStats stats = getPlayerData().getStats();
		if (!hasEnoughResources(1 / getValue(stats.getStat(ItemStat.ATTACK_SPEED), MMOItems.plugin.getConfig().getDouble("default.attack-speed")), CooldownType.ATTACK, false))
			return;

		UntargetedDurabilityItem durItem = new UntargetedDurabilityItem(getPlayer(), getNBTItem(), slot);
		if (durItem.isValid())
			durItem.decreaseDurability(1).update();

		// consume arrow
		// has to be after the CD check
		if (getPlayer().getGameMode() != GameMode.CREATIVE)
			getPlayer().getInventory().removeItem(new ItemStack(Material.ARROW));

		getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ENTITY_ARROW_SHOOT, 1, 1);
		Arrow arrow = getPlayer().launchProjectile(Arrow.class);
		arrow.setVelocity(getPlayer().getEyeLocation().getDirection().multiply(3 * getValue(getNBTItem().getStat(ItemStat.ARROW_VELOCITY), 1)));
		getPlayer().setVelocity(getPlayer().getVelocity().setX(0).setZ(0));

		MMOItems.plugin.getEntities().registerCustomProjectile(getNBTItem(), stats.newTemporary(), arrow, true);
	}
}
