package net.Indyuce.mmoitems.api.crafting;

import io.lumine.mythic.lib.MythicLib;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class CraftingStationCommand extends Command {
    private final String permission, noPerm, notAPlayer;

    @Nullable(value = "null when unregistered")
    private CraftingStation station;

    public CraftingStationCommand(CraftingStation station, String command, ConfigurationSection config) {
        super(command,
                config.getString("description", "Open station " + station.getName()),
                config.getString("usage", "/" + command),
                config.getStringList("aliases"));

        this.station = station;
        this.permission = config.getString("permission");
        this.noPerm = message(config, "no-perm");
        this.notAPlayer = message(config, "players-only");
    }

    @Nullable
    public CraftingStation getStation() {
        return station;
    }

    public void updateStation(@Nullable CraftingStation station) {
        this.station = station;
    }

    private String message(ConfigurationSection config, String path) {
        return config.contains("message." + path) ? MythicLib.plugin.parseColors(config.getString("message." + path)) : null;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String command, @NotNull String[] args) {

        // Player check
        if (!(sender instanceof Player)) {
            if (notAPlayer != null) sender.sendMessage(notAPlayer);
            return false;
        }

        // Permission check
        if (permission != null && !sender.hasPermission(permission)) {
            if (noPerm != null) sender.sendMessage(noPerm);
            return false;
        }

        // Open
        Objects.requireNonNull(station, "Internal error").getEditableView().generate((Player) sender).open();
        return true;
    }
}
