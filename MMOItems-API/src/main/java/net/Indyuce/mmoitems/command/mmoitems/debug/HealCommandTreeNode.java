package net.Indyuce.mmoitems.command.mmoitems.debug;

import io.lumine.mythic.lib.version.VPotionEffectType;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import io.lumine.mythic.lib.command.api.CommandTreeNode;

public class HealCommandTreeNode extends CommandTreeNode {
	public HealCommandTreeNode(CommandTreeNode parent) {
		super(parent, "heal");
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This command is only for players.");
			return CommandResult.FAILURE;
		}

		Player player = (Player) sender;
		player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
		player.setFoodLevel(20);
		player.setFireTicks(0);
		player.setSaturation(12);
		for (PotionEffectType pe : new PotionEffectType[] { PotionEffectType.POISON, PotionEffectType.BLINDNESS, VPotionEffectType
				.NAUSEA.get(),
				PotionEffectType.HUNGER, PotionEffectType.WEAKNESS, VPotionEffectType.SLOWNESS.get(), PotionEffectType.MINING_FATIGUE })
			player.removePotionEffect(pe);
		return CommandResult.SUCCESS;
	}
}
