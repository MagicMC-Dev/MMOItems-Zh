package net.Indyuce.mmoitems.command.mmoitems;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.command.api.CommandTreeNode;
import io.lumine.mythic.lib.command.api.Parameter;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.command.MMOItemsCommandTreeRoot;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class TakeCommandTreeNode extends CommandTreeNode {
    private static final Random random = new Random();

    public TakeCommandTreeNode(CommandTreeNode parent) {
        super(parent, "take");

        addParameter(MMOItemsCommandTreeRoot.TYPE);
        addParameter(MMOItemsCommandTreeRoot.ID_2);
        addParameter(Parameter.PLAYER);
        addParameter(Parameter.AMOUNT);
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (args.length != 5)
            return CommandResult.THROW_USAGE;

        try {

            // Target item & player
            final Type type = MMOItems.plugin.getTypes().getOrThrow(args[1].toUpperCase().replace("-", "_"));
            final String id = args[2].toUpperCase().replace("-", "_");
            final Player target = Bukkit.getPlayer(args[3]);
            Validate.notNull(target, "Could not find player called '" + args[3] + "'.");

            int amountLeft = Integer.parseInt(args[4]);
            for (int i = 0; i < target.getInventory().getSize() && amountLeft > 0; i++) {
                final ItemStack item = target.getInventory().getItem(i);
                if (item == null || item.getType() == Material.AIR)
                    continue;

                final NBTItem nbtItem = NBTItem.get(item);
                final String currentType = nbtItem.getType();
                if (type.getId().equals(currentType) && nbtItem.getString("MMOITEMS_ITEM_ID").equals(id)) {
                    final int removedAmount = Math.min(amountLeft, item.getAmount());
                    amountLeft -= removedAmount;
                    item.setAmount(item.getAmount() - removedAmount);
                    target.getInventory().setItem(i, item);
                }
            }

            return CommandResult.SUCCESS;

        } catch (IllegalArgumentException exception) {
            sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + exception.getMessage());
            return CommandResult.FAILURE;
        }
    }
}