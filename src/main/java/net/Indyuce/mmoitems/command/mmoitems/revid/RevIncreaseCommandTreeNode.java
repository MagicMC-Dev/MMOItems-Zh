package net.Indyuce.mmoitems.command.mmoitems.revid;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.mmogroup.mmolib.command.api.CommandTreeNode;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class RevIncreaseCommandTreeNode extends CommandTreeNode {
	public RevIncreaseCommandTreeNode(CommandTreeNode parent) {
		super(parent, "increase");

		addParameter(RevisionIDCommandTreeNode.TYPE_OR_ALL);
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		if (args.length < 3) return CommandResult.THROW_USAGE;

		if (!Type.isValid(args[2]) && !args[2].equalsIgnoreCase("all")) {
			sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "There is no item type called " + args[2].toUpperCase().replace("-", "_") + ".");
			sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "Type " + ChatColor.GREEN + "/mi list type" + ChatColor.RED + " to see all the available item types.");
			return CommandResult.FAILURE;
		}

		Type type = args[2].equalsIgnoreCase("all") ? null : Type.get(args[2]);
		List<MMOItemTemplate> templates = new ArrayList<>(type == null ? MMOItems.plugin.getTemplates().collectTemplates() : MMOItems.plugin.getTemplates().getTemplates(type));
		for(MMOItemTemplate template : templates) {
			ConfigFile file = template.getType().getConfigFile();
			file.getConfig().getConfigurationSection(template.getId() + ".base")
				.set("revision-id", Math.min(template.getRevisionId() + 1, Integer.MAX_VALUE));
			file.registerTemplateEdition(template);
		}

		sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.GREEN + "Successfully increased Rev IDs" + (type != null ? " for " + type.getName() : "") + "!");
		return CommandResult.SUCCESS;
	}
}
