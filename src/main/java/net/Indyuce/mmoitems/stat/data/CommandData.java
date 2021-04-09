package net.Indyuce.mmoitems.stat.data;

import org.apache.commons.lang.Validate;

public class CommandData {
	private final String command;
	private final double delay;
	private final boolean console, op;

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof CommandData)) { return false; }

		// Any difference in these will cause this to not be equal
		if (((CommandData) obj).getDelay() != getDelay()) { return false; }
		if (((CommandData) obj).isConsoleCommand() != isConsoleCommand()) { return false; }
		if (((CommandData) obj).hasOpPerms() != hasOpPerms()) { return false; }

		// Finally, if the command strings match.
		return ((CommandData) obj).getCommand().equals(getCommand());
	}

	public CommandData(String command, double delay, boolean console, boolean op) {
		Validate.notNull(command, "Command cannot be null");

		this.command = command;
		this.delay = delay;
		this.console = console;
		this.op = op;
	}

	public String getCommand() {
		return command;
	}

	public double getDelay() {
		return delay;
	}

	public boolean hasDelay() {
		return delay > 0;
	}

	public boolean isConsoleCommand() {
		return console;
	}

	public boolean hasOpPerms() {
		return op;
	}
}