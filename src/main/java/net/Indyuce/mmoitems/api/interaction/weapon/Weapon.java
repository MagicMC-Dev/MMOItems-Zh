package net.Indyuce.mmoitems.api.interaction.weapon;

import javax.annotation.Nullable;

import net.Indyuce.mmoitems.comp.flags.FlagPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.interaction.UseItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.PlayerData.CooldownType;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.comp.flags.FlagPlugin.CustomFlag;
import io.lumine.mythic.lib.api.item.NBTItem;

public class Weapon extends UseItem {
	public Weapon(Player player, NBTItem item) {
		this(PlayerData.get(player), item);
	}

	public Weapon(PlayerData playerData, NBTItem item) {
		super(playerData, item);
	}

	@Override
	public boolean applyItemCosts() {
		if (MMOUtils.twoHandedCase(getPlayer())) {
			Message.HANDS_TOO_CHARGED.format(ChatColor.RED).send(getPlayer(), "two-handed");
			return false;
		}

		boolean asCanUse = playerData.getRPG().canUse(getNBTItem(), true);
		boolean asFlagAllowed = true; FlagPlugin fg = MMOItems.plugin.getFlags(); if (fg != null) { asFlagAllowed = fg.isFlagAllowed(getPlayer(), CustomFlag.MI_WEAPONS); }
		else { MMOItems.Log("Flag Plugin Not Found");}
		return asCanUse || asFlagAllowed;
	}

	/**
	 * Applies mana and stamina weapon costs
	 * 
	 * @return If the attack was cast successfully
	 */
	public boolean applyWeaponCosts() {
		return applyWeaponCosts(0, null);
	}

	/**
	 * Applies cooldown, mana and stamina weapon costs
	 * 
	 * @param  attackSpeed The weapon attack speed
	 * @param  cooldown    The weapon cooldown type. When set to null, no
	 *                     cooldown will be applied. This is made to handle
	 * @return             If the attack was cast successfully
	 */
	public boolean applyWeaponCosts(double attackSpeed, @Nullable CooldownType cooldown) {
		if (cooldown != null && getPlayerData().isOnCooldown(cooldown))
			return false;

		double manaCost = getNBTItem().getStat("MANA_COST");
		if (manaCost > 0 && playerData.getRPG().getMana() < manaCost) {
			Message.NOT_ENOUGH_MANA.format(ChatColor.RED).send(getPlayer(), "not-enough-mana");
			return false;
		}

		double staminaCost = getNBTItem().getStat("STAMINA_COST");
		if (staminaCost > 0 && playerData.getRPG().getStamina() < staminaCost) {
			Message.NOT_ENOUGH_STAMINA.format(ChatColor.RED).send(getPlayer(), "not-enough-stamina");
			return false;
		}

		if (manaCost > 0)
			playerData.getRPG().giveMana(-manaCost);

		if (staminaCost > 0)
			playerData.getRPG().giveStamina(-staminaCost);

		if (cooldown != null)
			getPlayerData().applyCooldown(cooldown, attackSpeed);

		return true;
	}

	public ItemAttackResult handleTargetedAttack(CachedStats stats, LivingEntity target, ItemAttackResult result) {

		/*
		 * Handle weapon cooldown, mana and stamina costs
		 */
		double attackSpeed = getNBTItem().getStat(ItemStats.ATTACK_SPEED.getId());
		attackSpeed = attackSpeed == 0 ? 1.493 : 1 / attackSpeed;
		if (!applyWeaponCosts())
			return result.setSuccessful(false);

		/*
		 * Handle item set attack effects
		 */
		if (getMMOItem().getType().getItemSet().hasAttackEffect() && !getNBTItem().getBoolean("MMOITEMS_DISABLE_ATTACK_PASSIVE"))
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
