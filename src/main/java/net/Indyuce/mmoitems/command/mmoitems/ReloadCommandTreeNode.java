package net.Indyuce.mmoitems.command.mmoitems;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import net.Indyuce.mmoitems.MMOItems;
import net.mmogroup.mmolib.command.api.CommandTreeNode;

public class ReloadCommandTreeNode extends CommandTreeNode {
	public ReloadCommandTreeNode(CommandTreeNode parent) {
		super(parent, "reload");

		addChild(new StationsCommandTreeNode(this));
		addChild(new RecipesCommandTreeNode(this));
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		MMOItems.plugin.getLanguage().reload();
		MMOItems.plugin.getDropTables().reload();
		MMOItems.plugin.getTypes().reload();
		MMOItems.plugin.getTiers().reload();
		MMOItems.plugin.getSets().reload();
		MMOItems.plugin.getUpgrades().reload();
		MMOItems.plugin.getTemplates().reload();
		MMOItems.plugin.getWorldGen().reload();
		MMOItems.plugin.getCustomBlocks().reload();
		sender.sendMessage(
				MMOItems.plugin.getPrefix() + MMOItems.plugin.getName() + " " + MMOItems.plugin.getDescription().getVersion() + " reloaded.");
		sender.sendMessage(
				MMOItems.plugin.getPrefix() + "- " + ChatColor.RED + MMOItems.plugin.getTypes().getAll().size() + ChatColor.GRAY + " Item Types");
		sender.sendMessage(
				MMOItems.plugin.getPrefix() + "- " + ChatColor.RED + MMOItems.plugin.getTiers().getAll().size() + ChatColor.GRAY + " Item Tiers");
		sender.sendMessage(
				MMOItems.plugin.getPrefix() + "- " + ChatColor.RED + MMOItems.plugin.getSets().getAll().size() + ChatColor.GRAY + " Item Sets");
		return CommandResult.SUCCESS;
	}

	public class RecipesCommandTreeNode extends CommandTreeNode {
		public RecipesCommandTreeNode(CommandTreeNode parent) {
			super(parent, "recipes");
		}

		@Override
		public CommandResult execute(CommandSender sender, String[] args) {
			MMOItems.plugin.getRecipes().reloadRecipes();
			sender.sendMessage(MMOItems.plugin.getPrefix() + "Successfully reloaded recipes.");
			sender.sendMessage(MMOItems.plugin.getPrefix() + "- " + ChatColor.RED
					+ (MMOItems.plugin.getRecipes().getLoadedRecipes().size() + MMOItems.plugin.getRecipes().getCustomRecipes().size())
					+ ChatColor.GRAY + " Recipes");
			return CommandResult.SUCCESS;
		}
	}

	public class StationsCommandTreeNode extends CommandTreeNode {
		public StationsCommandTreeNode(CommandTreeNode parent) {
			super(parent, "stations");
		}

		@Override
		public CommandResult execute(CommandSender sender, String[] args) {
			MMOItems.plugin.getCrafting().reload();
			sender.sendMessage(MMOItems.plugin.getPrefix() + "Successfully reloaded the crafting stations..");
			sender.sendMessage(MMOItems.plugin.getPrefix() + "- " + ChatColor.RED + MMOItems.plugin.getCrafting().getAll().size() + ChatColor.GRAY
					+ " Crafting Stations");
			sender.sendMessage(
					MMOItems.plugin.getPrefix() + "- " + ChatColor.RED + MMOItems.plugin.getCrafting().countRecipes() + ChatColor.GRAY + " Recipes");
			return CommandResult.SUCCESS;
		}
	}
}
