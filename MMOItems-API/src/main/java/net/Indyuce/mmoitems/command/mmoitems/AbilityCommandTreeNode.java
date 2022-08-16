package net.Indyuce.mmoitems.command.mmoitems;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.command.api.CommandTreeNode;
import io.lumine.mythic.lib.command.api.Parameter;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.skill.trigger.TriggerMetadata;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.skill.RegisteredSkill;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AbilityCommandTreeNode extends CommandTreeNode {
	public AbilityCommandTreeNode(CommandTreeNode parent) {
		super(parent, "ability");

		addParameter(new Parameter("<ability>",
				(explorer, list) -> MMOItems.plugin.getSkills().getAll().forEach(ability -> list.add(ability.getHandler().getId()))));
		addParameter(Parameter.PLAYER_OPTIONAL);

		for (int j = 0; j < 10; j++) {
			addParameter(new Parameter("<modifier>", (explorer, list) -> {
				try {
					RegisteredSkill ability = MMOItems.plugin.getSkills().getSkillOrThrow(explorer.getArguments()[1].toUpperCase().replace("-", "_"));
					list.addAll(ability.getHandler().getModifiers());
				} catch (Exception ignored) {
				}
			}));
			addParameter(new Parameter("<value>", (explorer, list) -> list.add("0")));
		}
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		if (args.length < 2)
			return CommandResult.THROW_USAGE;

		if (args.length < 3 && !(sender instanceof Player)) {
			sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "Please specify a player to use this command.");
			return CommandResult.FAILURE;
		}

		// target
		Player target = args.length > 2 ? Bukkit.getPlayer(args[2]) : (Player) sender;
		if (target == null) {
			sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "Couldn't find player called " + args[2] + ".");
			return CommandResult.FAILURE;
		}

		// ability
		String key = args[1].toUpperCase().replace("-", "_");
		if (!MMOItems.plugin.getSkills().hasSkill(key)) {
			sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "Couldn't find ability " + key + ".");
			return CommandResult.FAILURE;
		}

		// modifiers
		AbilityData ability = new AbilityData(MMOItems.plugin.getSkills().getSkill(key), TriggerType.CAST);
		for (int j = 3; j < args.length - 1; j += 2) {
			String name = args[j];
			String value = args[j + 1];

			try {
				ability.setModifier(name, Double.parseDouble(value));
			} catch (Exception e) {
				sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "Wrong format: {" + name + " " + value + "}");
				return CommandResult.FAILURE;
			}
		}

		PlayerMetadata caster = MMOPlayerData.get(target).getStatMap().cache(EquipmentSlot.MAIN_HAND);
		ability.cast(new TriggerMetadata(caster, null, null));
		return CommandResult.SUCCESS;
	}
}
