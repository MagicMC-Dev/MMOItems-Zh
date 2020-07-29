package net.Indyuce.mmoitems.api.crafting.trigger;

import net.Indyuce.mmoitems.api.player.PlayerData;
import net.mmogroup.mmolib.api.MMOLineConfig;
import org.bukkit.Bukkit;

public class CommandTrigger extends Trigger {
    private final String command;

    public CommandTrigger(MMOLineConfig config) {
        super("command");

        config.validate("format");
        command = config.getString("format");
    }

    @Override
    public void whenCrafting(PlayerData data) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", data.getPlayer().getName()));
    }
}
