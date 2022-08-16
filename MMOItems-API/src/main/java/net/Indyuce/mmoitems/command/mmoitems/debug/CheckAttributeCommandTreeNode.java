package net.Indyuce.mmoitems.command.mmoitems.debug;

import java.text.DecimalFormat;
import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.lumine.mythic.lib.command.api.CommandTreeNode;
import io.lumine.mythic.lib.command.api.Parameter;

public class CheckAttributeCommandTreeNode extends CommandTreeNode {
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.####");

	public CheckAttributeCommandTreeNode(CommandTreeNode parent) {
		super(parent, "checkattribute");

		addParameter(
				new Parameter("<attribute>", (explorer, list) -> Arrays.asList(Attribute.values()).forEach(attribute -> list.add(attribute.name()))));
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This command is only for players.");
			return CommandResult.FAILURE;
		}

		if (args.length < 3)
			return CommandResult.THROW_USAGE;

		Player player = (Player) sender;
		try {
			AttributeInstance att = player.getAttribute(Attribute.valueOf(args[2].toUpperCase().replace("-", "_")));
			sender.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "--------------------------------------------------");
			sender.sendMessage(ChatColor.AQUA + "Default Value = " + ChatColor.RESET + att.getDefaultValue());
			sender.sendMessage(ChatColor.AQUA + "Base Value = " + ChatColor.RESET + att.getBaseValue());
			sender.sendMessage(ChatColor.AQUA + "Value = " + ChatColor.RESET + att.getValue());
			for (AttributeModifier mod : att.getModifiers())
				sender.sendMessage(
						mod.getName() + " " + DECIMAL_FORMAT.format(mod.getAmount()) + " " + mod.getOperation() + " " + mod.getSlot());
		} catch (IllegalArgumentException exception) {
			player.sendMessage("Couldn't find attribute.");
		}
		return CommandResult.SUCCESS;
	}
}
