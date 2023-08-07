package net.Indyuce.mmoitems.command.mmoitems.list;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.command.api.CommandTreeNode;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ListCommandTreeNode extends CommandTreeNode {
	public ListCommandTreeNode(CommandTreeNode parent) {
		super(parent, "list");

		addChild(new AbilityCommandTreeNode(this));
		addChild(new LuteAttackCommandTreeNode(this));
		addChild(new StaffSpiritCommandTreeNode(this));
		addChild(new TypeCommandTreeNode(this));
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		sender.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "-----------------[" + ChatColor.LIGHT_PURPLE + " MMOItems: lists "
				+ ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "]-----------------");
		sender.sendMessage(ChatColor.LIGHT_PURPLE + "/mi list type " + ChatColor.WHITE + "shows all item types (sword, axe...)");
		sender.sendMessage(ChatColor.LIGHT_PURPLE + "/mi list spirit " + ChatColor.WHITE + "shows all available staff spirits");
		sender.sendMessage(ChatColor.LIGHT_PURPLE + "/mi list lute " + ChatColor.WHITE + "shows all available lute attack effects");
		sender.sendMessage(ChatColor.LIGHT_PURPLE + "/mi list ability " + ChatColor.WHITE + "shows all available abilities");
		if (sender instanceof Player) {
			sender.sendMessage("");
			sender.sendMessage("Spigot Javadoc Links:");
			MythicLib.plugin.getVersion().getWrapper().sendJson((Player) sender, "[{\"text\":\"" + ChatColor.UNDERLINE + ChatColor.GREEN
					+ "Materials/Blocks\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\""
					+ ChatColor.GREEN + "Click to open webpage.\"}]}}},{\"text\":\" " + ChatColor.LIGHT_PURPLE + "- \"},{\"text\":\""
					+ ChatColor.UNDERLINE + ChatColor.GREEN
					+ "Potion Effects\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionEffectType.html\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\""
					+ ChatColor.GREEN + "Click to open webpage.\"}]}}},{\"text\":\" " + ChatColor.LIGHT_PURPLE + "- \"},{\"text\":\""
					+ ChatColor.UNDERLINE + ChatColor.GREEN
					+ "Sounds\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\""
					+ ChatColor.GREEN + "Click to open webpage.\"}]}}}]");
			MythicLib.plugin.getVersion().getWrapper().sendJson((Player) sender, "[{\"text\":\"" + ChatColor.UNDERLINE + ChatColor.GREEN
					+ "Entities/Mobs\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/EntityType.html\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\""
					+ ChatColor.GREEN + "Click to open webpage.\"}]}}},{\"text\":\" " + ChatColor.LIGHT_PURPLE + "- \"},{\"text\":\""
					+ ChatColor.UNDERLINE + ChatColor.GREEN
					+ "Enchantments\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://hub.spigotmc.org/javadocs/spigot/org/bukkit/enchantments/Enchantment.html\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\""
					+ ChatColor.GREEN + "Click to open webpage.\"}]}}},{\"text\":\" " + ChatColor.LIGHT_PURPLE + "- \"},{\"text\":\""
					+ ChatColor.UNDERLINE + ChatColor.GREEN
					+ "Particles\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Particles.html\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\""
					+ ChatColor.GREEN + "Click to open webpage.\"}]}}}]");
		}
		return CommandResult.SUCCESS;
	}
}
