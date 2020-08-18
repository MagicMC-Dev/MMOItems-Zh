package net.Indyuce.mmoitems.api.interaction.weapon;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.interaction.UseItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.PlayerData.CooldownType;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.comp.flags.FlagPlugin.CustomFlag;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.api.item.NBTItem;

public class Weapon extends UseItem {
	public Weapon(Player player, NBTItem item) {
		this(PlayerData.get(player), item);
	}

	public Weapon(PlayerData playerData, NBTItem item) {
		super(playerData, item);
	}

	@Override
	public boolean canBeUsed() {
		if (MMOUtils.twoHandedCase(getPlayer())) {
			Message.HANDS_TOO_CHARGED.format(ChatColor.RED).send(getPlayer(), "two-handed");
			return false;
		}

		return MMOItems.plugin.getFlags().isFlagAllowed(getPlayer(), CustomFlag.MI_WEAPONS) && playerData.getRPG().canUse(getNBTItem(), true);
	}

	/*
	 * applies the cooldown, mana & stamina cost and returns a true boolean if
	 * the player does have all the resource requirements
	 */
	public boolean hasEnoughResources(double attackSpeed, CooldownType cooldown, boolean isSwing) {
		if (!isSwing && getPlayerData().isOnCooldown(cooldown))
			return false;

		double manaCost = getNBTItem().getStat(ItemStat.MANA_COST), staminaCost = getNBTItem().getStat(ItemStat.STAMINA_COST);

		if (manaCost > 0 && playerData.getRPG().getMana() < manaCost) {
			Message.NOT_ENOUGH_MANA.format(ChatColor.RED).send(getPlayer(), "not-enough-mana");
			return false;
		}

		if (staminaCost > 0 && playerData.getRPG().getStamina() < staminaCost) {
			Message.NOT_ENOUGH_STAMINA.format(ChatColor.RED).send(getPlayer(), "not-enough-stamina");
			return false;
		}

		if (manaCost > 0)
			playerData.getRPG().giveMana(-manaCost);

		if (staminaCost > 0)
			playerData.getRPG().giveStamina(-staminaCost);

		getPlayerData().applyCooldown(cooldown, attackSpeed);
		return true;
	}

	public ItemAttackResult targetedAttack(CachedStats stats, LivingEntity target, ItemAttackResult result) {

		// cooldown
		double attackSpeed = getNBTItem().getStat(ItemStat.ATTACK_SPEED);
		attackSpeed = attackSpeed == 0 ? 1.493 : 1 / attackSpeed;
		if (!hasEnoughResources(attackSpeed, CooldownType.ATTACK, true))
			return result.setSuccessful(false);

		if (!getNBTItem().getBoolean("MMOITEMS_DISABLE_ATTACK_PASSIVE") && getMMOItem().getType().getItemSet().hasAttackEffect())
			getMMOItem().getType().getItemSet().applyAttackEffect(stats, target, this, result);

		return result;
	}

	protected Location getGround(Location loc) {
		for (int j = 0; j < 20; j++) {
			if (loc.getBlock().getType().isSolid())
				return loc;
			loc.add(0, -1, 0);
		}
		return loc;
	}

	// returns default getValue if stat equals 0
	public double getValue(double a, double def) {
		return a <= 0 ? def : a;
	}
}
