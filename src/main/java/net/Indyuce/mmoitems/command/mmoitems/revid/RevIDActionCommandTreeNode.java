package net.Indyuce.mmoitems.command.mmoitems.revid;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import io.lumine.mythic.lib.command.api.CommandTreeNode;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class RevIDActionCommandTreeNode extends CommandTreeNode {
	private final String cmdType;
	private final Function<Integer, Integer> modifier;

	public RevIDActionCommandTreeNode(CommandTreeNode parent, String type, Function<Integer, Integer> modifier) {
		super(parent, type);

		this.cmdType = type;
		this.modifier = modifier;
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
		int failed = 0;
		for(MMOItemTemplate template : templates) {
			ConfigFile file = template.getType().getConfigFile();
			if(!file.getConfig().isConfigurationSection(template.getId() + ".base"))
				failed++;
			else {
				file.getConfig().getConfigurationSection(template.getId() + ".base").set("revision-id", modifier.apply(template.getRevisionId()));
				file.registerTemplateEdition(template);
			}
		}

		if(failed > 0) sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "Couldn't find ConfigurationSection for " + failed + " of the specified items.");
		else sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.GREEN + "Successfully " + cmdType + "d Rev IDs" + (type != null ? " for " + type.getName() : "") + "!");
		return CommandResult.SUCCESS;
	}
}
