package net.Indyuce.mmoitems.command.mmoitems;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.mmolibcommands.api.CommandTreeNode;
import io.lumine.mythic.lib.mmolibcommands.api.Parameter;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.ability.Ability;
import net.Indyuce.mmoitems.api.ability.Ability.CastingMode;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AbilityCommandTreeNode extends CommandTreeNode {
	public AbilityCommandTreeNode(CommandTreeNode parent) {
		super(parent, "ability");

		addParameter(new Parameter("<ability>",
				(explorer, list) -> MMOItems.plugin.getAbilities().getAll().forEach(ability -> list.add(ability.getID()))));
		addParameter(Parameter.PLAYER_OPTIONAL);

		// three modifiers but more can be used
		for (int j = 0; j < 3; j++) {
			addParameter(new Parameter("<modifier>", (explorer, list) -> {
				try {
					Ability ability = MMOItems.plugin.getAbilities().getAbility(explorer.getArguments()[1].toUpperCase().replace("-", "_"));
					list.addAll(ability.getModifiers());
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
		if (!MMOItems.plugin.getAbilities().hasAbility(key)) {
			sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "Couldn't find ability " + key + ".");
			return CommandResult.FAILURE;
		}

		// modifiers
		AbilityData ability = new AbilityData(MMOItems.plugin.getAbilities().getAbility(key), CastingMode.RIGHT_CLICK);
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

		PlayerData data = PlayerData.get(target);
		data.cast(data.getStats().newTemporary(EquipmentSlot.MAIN_HAND), null, new ItemAttackResult(0), ability);
		return CommandResult.SUCCESS;
	}
}
