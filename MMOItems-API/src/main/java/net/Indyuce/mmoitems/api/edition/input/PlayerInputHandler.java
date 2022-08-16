package net.Indyuce.mmoitems.api.edition.input;

import org.bukkit.entity.Player;

import net.Indyuce.mmoitems.api.edition.Edition;

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
	 * because it should only be ran by edition process classes
	 * 
	 * @param input Player input
	 */
	protected void registerInput(String input) {
		if (!edition.processInput(input))
			return;

		if (edition.shouldGoBack())
			edition.getInventory().open();
		close();
	}

	public abstract void close();
}
