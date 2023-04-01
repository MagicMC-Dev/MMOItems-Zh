package net.Indyuce.mmoitems.api.util.message;

import io.lumine.mythic.lib.MythicLib;
import net.Indyuce.mmocore.api.player.PlayerActivity;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmoitems.MMOItems;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FormattedMessage {
    private final boolean actionBar;

    @NotNull
    private String message;

    /**
     * Used to block empty messages from spamming the chat. Also used to apply
     * a default color code to the message and be able to select which
     * messages are displayed on the action bar and which go to chat.
     *
     * @param message Unformatted message
     */
    public FormattedMessage(Message message) {
        this.message = message.getUpdated();
        this.actionBar = MMOItems.plugin.getConfig().getBoolean("action-bar-display." + message.getActionBarConfigPath());
    }

    /**
     * Can be used by external plugins. This can be useful to
     * apply that 60 tick action bar timeout when using MMOCore
     *
     * @param message   Messages with color codes applied.
     * @param actionBar Should the message be displayed on the action bar
     */
    public FormattedMessage(String message, boolean actionBar) {
        this.message = message;
        this.actionBar = actionBar;
    }

    @NotNull
    public FormattedMessage format(ChatColor prefix, String... toReplace) {

        // Compatibility with #send(Player)
        if (message.isEmpty())
            return this;

        message = prefix + message;
        for (int j = 0; j < toReplace.length; j += 2)
            message = message.replace(toReplace[j], toReplace[j + 1]);
        return this;
    }

    /**
     * Either sends to action bar or to chat if it's not empty.
     * Supports MiniMessage since 6.8.3 snapshots
     *
     * @param player Player receiving the message
     */
    public void send(Player player) {
        if (message.isEmpty())
            return;

        if (actionBar) {
            if (Bukkit.getPluginManager().isPluginEnabled("MMOCore"))
                PlayerData.get(player).setLastActivity(PlayerActivity.ACTION_BAR_MESSAGE);

            MythicLib.plugin.getVersion().getWrapper().sendActionBar(player, message);
        } else
            player.sendMessage(message);
    }

    @Override
    public String toString() {
        return message;
    }
}
