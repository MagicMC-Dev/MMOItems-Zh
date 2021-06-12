package net.Indyuce.mmoitems.stat.data;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;

import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;

public class CommandListData implements StatData, Mergeable, RandomStatData {
	@NotNull private final Set<CommandData> commands;

	public CommandListData(@NotNull Set<CommandData> commands) {
		this.commands = commands;
	}

	public CommandListData(CommandData... commands) {
		this(new HashSet<>());

		add(commands);
	}

	public void add(CommandData... commands) {
		this.commands.addAll(Arrays.asList(commands));
	}

	@NotNull public Set<CommandData> getCommands() {
		return commands;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof CommandListData)) { return false; }
		if (((CommandListData) obj).getCommands().size() != getCommands().size()) { return false; }

		for (CommandData objCommand : ((CommandListData) obj).getCommands()) {

			if (objCommand == null) { continue; }

			// Compare to mine
			boolean unmatched = true;
			for (CommandData thisCommand : getCommands()) {

				// Unequal? Fail
				if (objCommand.equals(thisCommand)) { unmatched = false; break; } }

			if (unmatched) { return false; }
		}

		// Success
		return true;
	}

	@Override
	public void merge(StatData data) {
		Validate.isTrue(data instanceof CommandListData, "Cannot merge two different stat data types");
		commands.addAll(((CommandListData) data).commands);
	}

	@Override
	public @NotNull StatData cloneData() {
		return new CommandListData(commands);
	}

	@Override
	public boolean isClear() { return getCommands().size() == 0; }

	@Override
	public StatData randomize(MMOItemBuilder builder) {
		return new CommandListData(new HashSet<>(commands));
	}
}