package net.Indyuce.mmoitems.command.mmoitems;

import java.util.Arrays;
import java.util.Random;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.template.loot.ClassFilter;
import net.Indyuce.mmoitems.api.item.template.loot.LootBuilder;
import net.Indyuce.mmoitems.api.item.template.loot.TypeFilter;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.mmogroup.mmolib.api.util.SmartGive;
import net.mmogroup.mmolib.command.api.CommandTreeNode;
import net.mmogroup.mmolib.command.api.Parameter;

public class GenerateCommandTreeNode extends CommandTreeNode {
	private static final Random random = new Random();

	public GenerateCommandTreeNode(CommandTreeNode parent) {
		super(parent, "generate");

		addParameter(Parameter.PLAYER);
		addParameter(new Parameter("(extra-args)", (explorer, list) -> list
				.addAll(Arrays.asList("-matchlevel", "-matchclass", "-level:", "-class:", "-type:", "-id:", "-tier:", "-gimme"))));
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		try {
			final Player target = Bukkit.getPlayer(args[1]);
			Validate.notNull(target, "Could not find player called " + args[1] + ".");

			GenerateCommandHandler handler = new GenerateCommandHandler(args);

			final Player give = handler.hasArgument("gimme") || handler.hasArgument("giveme") ? (sender instanceof Player ? (Player) sender : null)
					: target;
			Validate.notNull(give, "You cannot use -gimme");

			RPGPlayer rpgPlayer = PlayerData.get(target).getRPG();
			final int itemLevel = handler.hasArgument("level") ? Integer.parseInt(handler.getValue("level"))
					: (handler.hasArgument("matchlevel") ? MMOItems.plugin.getTemplates().rollLevel(rpgPlayer.getLevel()) : 1 + random.nextInt(100));
			final ItemTier itemTier = handler.hasArgument("tier")
					? MMOItems.plugin.getTiers().getOrThrow(handler.getValue("tier").toUpperCase().replace("-", "_"))
					: MMOItems.plugin.getTemplates().rollTier();

			LootBuilder builder = new LootBuilder(itemLevel, itemTier);
			if (handler.hasArgument("matchclass"))
				builder.applyFilter(new ClassFilter(rpgPlayer));
			if (handler.hasArgument("class"))
				builder.applyFilter(new ClassFilter(handler.getValue("class").replace("-", " ").replace("_", " ")));
			if (handler.hasArgument("type")) {
				String format = handler.getValue("type");
				Validate.isTrue(Type.isValid(format), "Could not find type with ID '" + format + "'");
				builder.applyFilter(new TypeFilter(Type.get(format)));
			}

			MMOItem mmoitem = builder.rollLoot();
			Validate.notNull(mmoitem, "No item matched your criterias.");

			ItemStack item = mmoitem.newBuilder().build();
			Validate.isTrue(item != null && item.getType() != Material.AIR, "Could not generate item with ID '" + mmoitem.getId() + "'");
			new SmartGive(give).give(item);
			return CommandResult.SUCCESS;

		} catch (IllegalArgumentException exception) {
			sender.sendMessage(ChatColor.RED + exception.getMessage());
			return CommandResult.FAILURE;
		}
	}
}
