package net.Indyuce.mmoitems.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.util.AltChar;

public class PluginHelp {
	private static final int commandsPerPage = 8;

	private final CommandSender sender;

	public PluginHelp(CommandSender sender) {
		this.sender = sender;
	}

	public void open(int page) {
		if (page < 1 || (page - 1) * commandsPerPage >= PluginCommand.values().length)
			return;

		for (int j = 0; j < 10; j++)
			sender.sendMessage("");
		sender.sendMessage("" + ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH + "--------------------" + ChatColor.DARK_GRAY + "["
				+ ChatColor.LIGHT_PURPLE + " MMOItems Help " + ChatColor.DARK_GRAY + "]" + ChatColor.STRIKETHROUGH + "-------------------");

		if (sender instanceof Player) {
			int min = (page - 1) * commandsPerPage;
			int max = page * commandsPerPage;
			int n = 0;

			for (; min + n < max && min + n < PluginCommand.values().length && min + n > -1; n++)
				PluginCommand.values()[min + n].sendAsJson((Player) sender);

			while (n++ < commandsPerPage)
				sender.sendMessage("");

			MythicLib.plugin.getVersion().getWrapper().sendJson((Player) sender,
					"[{\"text\":\"" + ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH + "------------------" + ChatColor.DARK_GRAY + "[\"},{\"text\":\""
							+ ChatColor.RED + "««\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/mi help " + (page - 1)
							+ "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"Previous Page\"}}},{\"text\":\""
							+ ChatColor.DARK_GRAY + "]" + ChatColor.STRIKETHROUGH + "---" + ChatColor.DARK_GRAY + "(" + ChatColor.GREEN + page
							+ ChatColor.DARK_GRAY + "/" + ChatColor.GREEN + getMaxPage() + ChatColor.DARK_GRAY + ")" + ChatColor.STRIKETHROUGH + "---"
							+ ChatColor.DARK_GRAY + "[\"},{\"text\":\"" + ChatColor.GREEN
							+ "»»\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/mi help " + (page + 1)
							+ "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"Next Page\"}}},{\"text\":\"" + ChatColor.DARK_GRAY
							+ "]" + ChatColor.STRIKETHROUGH + "-----------------\"}]");
		} else {
			for (PluginCommand command : PluginCommand.values())
				command.sendAsMessage(sender);
		}
	}

	public static int getMaxPage() {
		return (int) Math.max(0, Math.ceil((double) PluginCommand.values().length / (double) commandsPerPage));
	}

	public enum PluginCommand {
		OPTIONAL("()" + ChatColor.WHITE + " = 可选"),
		REQUIRED("<>" + ChatColor.WHITE + " = 必填"),
		MULTIPLE_ARGS("..." + ChatColor.WHITE + " = 多个参数"),
		HOVER_COMMAND(ChatColor.WHITE + "将鼠标悬停在命令上可查看其描述"),
		TAB_INFO(ChatColor.WHITE + "键入命令时按 [tab] 即可自动完成"),
		SPACE(""),

		HELP("mi help <page>", "显示帮助页面"),
		GIVE("mi give <type> <item> (player) (min-max) (unident-chance) (drop-chance)",
				"&a/mi <type> <item> (player) (min-max) (unidentification-chance) (drop-chance)\\n&f向给予玩家物品\\n支持掉落几率、随机几率和随机数量"),
		GIVEALL("mi giveall <type> <item> <min-max> <unident-chance>", "给予服务器上的所有在线玩家物品"),
		BROWSE("mi browse (type)", "允许您浏览您创建的所有物品."),
		GENERATE("mi generate <player> (extra-args)", "生成一个随机物品\\n使用 /mi 生成以查看所有可用参数"),
		ABILITY("mi ability <ability> (player) (mod1) (val1) (mod2) (val2)...", "强制玩家施放技能"),
		DROP("mi drop <type> <item-id> <world-name> <x> <y> <z>...",
				"&a/mi drop <type> <item-id> <world-name> <x> <y> <z> <drop-chance> <[min]-[max]> <unidentified-chance>\\n&f将物品扔到目标位置"),
		HEAL("mi heal", "治愈你并消除负面药水效果"),
		IDENTIFY("mi item identifify", "识别持有的物品"),
		UNIDENTIFY("mi item unidentifify", "无法识别所持有的物品"),
		REPAIR("mi item repair", "修复持有的物品"),
		DECONSTRUCT("mi item deconstruct", "分解你所持有的物品"),
		INFO("mi debug info (player)", "显示指定玩家的信息"),
		LIST("mi list", "编辑物品时的一些有用的东西"),
		RELOAD("mi reload (adv-recipes/stations)", "重新加载特定/每个插件系统"),
		CREATE("mi create <type> <id>", "创建一个新物品"),
		COPY("mi copy <type> <target-id> <new-item-id>", "复制现有物品"),
		DELETE("mi delete <type> <id>", "从插件中删除一个物品."),
		EDIT("mi edit <type> <id>", "调出物品版本菜单"),
		ITEMLIST("mi itemlist <type>", "列出特定物品类型的所有物品"),
		ALLITEMS("mi allitems", "列出每种类型的物品"),
		STATIONS_LIST("mi stations list", "列出可用的制作站"),
		STATIONS_OPEN("mi stations open <station> (player)", "向玩家开放制作站"),

		UPDATEITEM("updateitem", "更新持有的物品."),
		UPDATEITEM_ITEM("updateitem <type> <id>", "启用特定物品的物品更新程序."),

		;

		private final String usage, help;

		PluginCommand(String line) {
			this(line, null);
		}

		PluginCommand(String usage, String help) {
			this.usage = usage;
			this.help = help == null ? null : ChatColor.translateAlternateColorCodes('&', help);
		}

		private boolean isCommand() {
			return help != null;
		}

		private void sendAsJson(Player player) {
			if (isCommand())
				MythicLib.plugin.getVersion().getWrapper().sendJson(player, "{\"text\":\"" + ChatColor.LIGHT_PURPLE + AltChar.listDash + ChatColor.GRAY
						+ " /" + usage + "\",\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"" + help + "\"}}}");
			else
				player.sendMessage(ChatColor.LIGHT_PURPLE + usage);
		}

		private void sendAsMessage(CommandSender sender) {
			if (isCommand())
				sender.sendMessage(ChatColor.LIGHT_PURPLE + "- /" + usage + ChatColor.WHITE + " | " + help);
			else
				sender.sendMessage(ChatColor.LIGHT_PURPLE + usage);
		}
	}
}
