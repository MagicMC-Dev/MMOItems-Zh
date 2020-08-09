package net.Indyuce.mmoitems.api.interaction;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.interaction.weapon.Gauntlet;
import net.Indyuce.mmoitems.api.interaction.weapon.Weapon;
import net.Indyuce.mmoitems.api.interaction.weapon.untargeted.Crossbow;
import net.Indyuce.mmoitems.api.interaction.weapon.untargeted.Lute;
import net.Indyuce.mmoitems.api.interaction.weapon.untargeted.Musket;
import net.Indyuce.mmoitems.api.interaction.weapon.untargeted.Staff;
import net.Indyuce.mmoitems.api.interaction.weapon.untargeted.Whip;
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.comp.flags.FlagPlugin.CustomFlag;
import net.Indyuce.mmoitems.stat.data.CommandData;
import net.Indyuce.mmoitems.stat.data.CommandListData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.api.item.NBTItem;

public class UseItem {
	protected final Player player;
	protected final PlayerData playerData;
	protected final LiveMMOItem mmoitem;

	protected static final Random random = new Random();

	public UseItem(Player player, NBTItem nbtItem, Type type) {
		this(PlayerData.get(player), nbtItem, type);
	}

	public UseItem(PlayerData playerData, NBTItem nbtItem, Type type) {
		this.player = playerData.getPlayer();
		this.playerData = playerData;
		this.mmoitem = new LiveMMOItem(nbtItem);
	}

	public Player getPlayer() {
		return player;
	}

	public PlayerData getPlayerData() {
		return playerData;
	}

	public MMOItem getMMOItem() {
		return mmoitem;
	}

	public NBTItem getNBTItem() {
		return mmoitem.getNBT();
	}

	public ItemStack getItem() {
		return mmoitem.getNBT().getItem();
	}

	public boolean canBeUsed() {
		return playerData.getRPG().canUse(mmoitem.getNBT(), true);
	}

	public void executeCommands() {
		if (MMOItems.plugin.getFlags().isFlagAllowed(player, CustomFlag.MI_COMMANDS) && mmoitem.hasData(ItemStat.COMMANDS))
			((CommandListData) mmoitem.getData(ItemStat.COMMANDS)).getCommands().forEach(command -> scheduleCommandExecution(command));
	}

	private void scheduleCommandExecution(CommandData command) {
		String parsed = MMOItems.plugin.getPlaceholderParser().parse(player, command.getCommand());

		if (!command.hasDelay())
			dispatchCommand(parsed, command.isConsoleCommand(), command.hasOpPerms());
		else
			Bukkit.getScheduler().runTaskLater(MMOItems.plugin, () -> dispatchCommand(parsed, command.isConsoleCommand(), command.hasOpPerms()),
					(long) command.getDelay() * 20);
	}

	private void dispatchCommand(String parsed, boolean console, boolean op) {
		if (console) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed);
			return;
		}

		if (op && !player.isOp()) {
			player.setOp(true);
			try {
				Bukkit.dispatchCommand(player, parsed);
			} catch (Exception e1) {
			}
			player.setOp(false);
		} else
			Bukkit.dispatchCommand(player, parsed);
	}

	public static UseItem getItem(Player player, NBTItem item, Type type) {
		if (type.corresponds(Type.CONSUMABLE))
			return new Consumable(player, item, type);
		if (type.corresponds(Type.SKIN))
			return new ItemSkin(player, item, type);
		if (type.corresponds(Type.GEM_STONE))
			return new GemStone(player, item, type);
		if (type.corresponds(Type.MUSKET))
			return new Musket(player, item, type);
		if (type.corresponds(Type.CROSSBOW))
			return new Crossbow(player, item, type);
		if (type.corresponds(Type.GAUNTLET))
			return new Gauntlet(player, item, type);
		if (type.corresponds(Type.WHIP))
			return new Whip(player, item, type);
		if (type.corresponds(Type.LUTE))
			return new Lute(player, item, type);
		if (type.corresponds(Type.STAFF))
			return new Staff(player, item, type);
		return type.isWeapon() ? new Weapon(player, item, type) : new UseItem(player, item, type);
	}
}
