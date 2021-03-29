package net.Indyuce.mmoitems.api.util.message;

import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackPalette;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

/**
 *   ' Presentation '
 */
@SuppressWarnings("unused")
public class FFPMMOItems extends FriendlyFeedbackPalette {

    /*
     *   The instance of this palette :p
     */
    FFPMMOItems() {}
    @NotNull static FFPMMOItems instance = new FFPMMOItems();
    @NotNull public static FFPMMOItems get() { return instance; }

    @NotNull @Override public String getBodyFormat() { return "§x§a§5§b§5§a§7"; }
    @NotNull @Override public String consoleBodyFormat() { return ChatColor.GRAY.toString(); }

    @NotNull @Override public String getExampleFormat() { return "§x§e§0§f§5§9§3"; }
    @NotNull @Override public String consoleExampleFormat() { return ChatColor.YELLOW.toString(); }

    @NotNull @Override public String getInputFormat() { return"§x§7§d§c§7§5§8"; }
    @NotNull @Override public String consoleInputFormat() { return ChatColor.GREEN.toString(); }

    @NotNull @Override public String getResultFormat() { return "§x§5§c§e§0§0§4"; }
    @NotNull @Override public String consoleResultFormat() { return ChatColor.GREEN.toString(); }

    @NotNull @Override public String getSuccessFormat() { return "§x§2§5§f§7§c§6"; }
    @NotNull @Override public String consoleSuccessFormat() { return ChatColor.AQUA.toString(); }

    @NotNull @Override public String getFailureFormat() { return "§x§f§f§6§0§2§6"; }
    @NotNull @Override public String consoleFailureFormat() { return ChatColor.RED.toString(); }

    @NotNull @Override public String getRawPrefix() { return "§8[§eMMOItems#s§8] "; }
    @NotNull @Override public String getRawPrefixConsole() { return "§8[§eMMOItems#s§8] "; }

    @NotNull @Override public String getSubdivisionFormat() { return "§x§c§c§a§3§3§3§o"; }
    @NotNull @Override public String consoleSubdivisionFormat() { return "§6§o"; }
}
