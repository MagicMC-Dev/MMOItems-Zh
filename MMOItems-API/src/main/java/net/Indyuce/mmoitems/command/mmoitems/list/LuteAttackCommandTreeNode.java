package net.Indyuce.mmoitems.command.mmoitems.list;

import io.lumine.mythic.lib.command.api.CommandTreeNode;
import net.Indyuce.mmoitems.stat.LuteAttackEffectStat.LuteAttackEffect;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class LuteAttackCommandTreeNode extends CommandTreeNode {
    public LuteAttackCommandTreeNode(CommandTreeNode parent) {
        super(parent, "lute");
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        sender.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "-----------------[" + ChatColor.LIGHT_PURPLE
                + " Lute Attack Effects " + ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "]-----------------");
        for (LuteAttackEffect lae : LuteAttackEffect.values())
            sender.sendMessage("* " + ChatColor.LIGHT_PURPLE + lae.name());
        return CommandResult.SUCCESS;
    }
}
