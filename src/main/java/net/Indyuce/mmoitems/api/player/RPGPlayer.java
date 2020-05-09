package net.Indyuce.mmoitems.api.player;

import java.text.DecimalFormat;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.Indyuce.mmoitems.stat.type.ItemRestriction;
import net.mmogroup.mmolib.api.item.NBTItem;

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
		if (item.hasTag("MMOITEMS_UNIDENTIFIED_ITEM")) {
			if (message) {
				Message.UNIDENTIFIED_ITEM.format(ChatColor.RED).send(player.getPlayer(), "cant-use-item");
				player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1.5f);
			}
			return false;
		}

		for (ItemRestriction condition : MMOItems.plugin.getStats().getItemRestrictionStats())
			if (!condition.canUse(this, item, message))
				return false;

		return true;
	}

	public boolean canCast(AbilityData data) {

		if (playerData.hasCooldownInfo(data.getAbility())) {
			CooldownInformation info = playerData.getCooldownInfo(data.getAbility());
			if (!info.hasCooledDown()) {
				if (data.getCastingMode().displaysMessage()) {
					String progressBar = ChatColor.YELLOW + "";
					double progress = (info.getInitialCooldown() - info.getRemaining()) / info.getInitialCooldown() * 10;

					String barChar = MMOItems.plugin.getConfig().getString("cooldown-progress-bar-char");
					for (int j = 0; j < 10; j++)
						progressBar += (progress >= j ? ChatColor.GREEN : ChatColor.WHITE) + barChar;
					Message.SPELL_ON_COOLDOWN.format(ChatColor.RED, "#left#", "" + new DecimalFormat("0.#").format(info.getRemaining()), "#progress#",
							progressBar, "#s#", (info.getRemaining() >= 2 ? "s" : "")).send(player, "ability-cooldown");
				}
				return false;
			}
		}

		if (MMOItems.plugin.getConfig().getBoolean("permissions.abilities")
				&& !player.hasPermission("mmoitems.ability." + data.getAbility().getLowerCaseID())
				&& !player.hasPermission("mmoitems.bypass.ability"))
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
