package net.Indyuce.mmoitems.command.mmoitems.debug;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.command.api.CommandTreeNode;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class TestCommandTreeNode extends CommandTreeNode {
    public TestCommandTreeNode(CommandTreeNode parent) {
        super(parent, "test");
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {

        ItemStack stack = new ItemStack(Material.DIAMOND);
        ItemMeta meta = stack.getItemMeta();
        List<String> lore = new ArrayList<>();
        for (int i = 0; i < 30; i++) lore.add(ChatColor.WHITE + "\u0274" + UtilityMethods.getFontSpace(i) + "\u0274" + " -> " + i);
        meta.setLore(lore);
        stack.setItemMeta(meta);

        ((Player) sender).getInventory().addItem(stack);

        return CommandResult.SUCCESS;
    }
}
