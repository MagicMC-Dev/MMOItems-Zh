package net.Indyuce.mmoitems.api.edition.input;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.Indyuce.mmoitems.api.edition.Edition;
import org.jetbrains.annotations.NotNull;

public abstract class PlayerInputHandler {

    /**
     * Saves the last inventory opened, the item data, and the last opened page;
     * allows for a much easier access to this data
     */
    private final Edition edition;

    /**
     * Abstract class which lists all possible ways to retrieve player input
     *
     * @param edition The edition object
     */
    public PlayerInputHandler(Edition edition) {
        this.edition = edition;
    }

    public Player getPlayer() {
        return edition.getInventory().getPlayer();
    }

    /**
     * Processes the player input, closes the edition process if needed and
     * opens the previously opened GUI if needed. This method is protected
     * because it should only be ran by edition process classes.
     * For security this method should be called on the main server thread.
     *
     * @param input Player input
     */
    protected void registerInput(@NotNull String input) {
        Validate.isTrue(Bukkit.isPrimaryThread(), "Input must be registered on primary thread");

        if (!edition.processInput(input))
            return;

        if (edition.shouldGoBack())
            edition.getInventory().open();
        close();
    }

    public abstract void close();
}
