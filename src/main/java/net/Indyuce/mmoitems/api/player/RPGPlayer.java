package net.Indyuce.mmoitems.api.player;

import java.text.DecimalFormat;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.Indyuce.mmoitems.stat.type.Conditional;

public abstract class RPGPlayer {
	private final PlayerData playerData;
	private final Player player;

	@Deprecated
	public RPGPlayer(Player player) {
		this(PlayerData.get(player));
	}

	public RPGPlayer(PlayerData playerData) {
		this.player = playerData.getPlayer();
		this.playerData = playerData;
	}

	public PlayerData getPlayerData() {
		return playerData;
	}

	public Player getPlayer() {
		return playerData.getPlayer();
	}

	public abstract int getLevel();

	public abstract String getClassName();

	public abstract double getMana();

	public abstract double getStamina();

	public abstract void setMana(double value);

	public abstract void setStamina(double value);

	public void giveMana(double value) {
		setMana(getMana() + value);
	}

	public void giveStamina(double value) {
		setStamina(getStamina() + value);
	}

	public boolean canUse(NBTItem item, boolean message) {

		for (Conditional condition : MMOItems.plugin.getStats().getConditionals())
			if (!condition.canUse(this, item, message))
				return false;

		return true;
	}

	public boolean canCast(AbilityData data, boolean message) {
		double remaining = playerData.getRemainingAbilityCooldown(data.getAbility());
		if (remaining > 0) {
			if (message) {
				String progressBar = ChatColor.YELLOW + "";
				double cooldown = data.getModifier("cooldown");
				double progress = (cooldown - remaining) / cooldown * 10.;

				String barChar = MMOItems.plugin.getConfig().getString("cooldown-progress-bar-char");
				for (int j = 0; j < 10; j++)
					progressBar += (progress >= j ? ChatColor.GREEN : ChatColor.WHITE) + barChar;
				Message.SPELL_ON_COOLDOWN.format(ChatColor.RED, "#left#", "" + new DecimalFormat("0.#").format(remaining), "#progress#", progressBar, "#s#", (remaining >= 2 ? "s" : "")).send(player, "ability-cooldown");
			}
			return false;
		}

		if (MMOItems.plugin.getConfig().getBoolean("permissions.abilities") && !player.hasPermission("mmoitems.ability." + data.getAbility().getLowerCaseID()) && !player.hasPermission("mmoitems.bypass.ability"))
			return false;

		if (data.hasModifier("mana") && getMana() < data.getModifier("mana")) {
			Message.NOT_ENOUGH_MANA.format(ChatColor.RED).send(player, "not-enough-mana");
			return false;
		}

		if (data.hasModifier("stamina") && getStamina() < data.getModifier("stamina")) {
			Message.NOT_ENOUGH_STAMINA.format(ChatColor.RED).send(player, "not-enough-stamina");
			return false;
		}

		return true;
	}

}
