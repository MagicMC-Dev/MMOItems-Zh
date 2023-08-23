package net.Indyuce.mmoitems.stat.data;

import java.util.*;

import org.apache.commons.lang.Validate;

import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import org.jetbrains.annotations.NotNull;

public class CommandListData implements StatData, Mergeable, RandomStatData<CommandListData> {
    @NotNull
    private final List<CommandData> commands;

    public CommandListData(@NotNull List<CommandData> commands) {
        this.commands = commands;
    }

    public CommandListData(CommandData... commands) {
        this(new ArrayList<>());

        add(commands);
    }

    public void add(CommandData... commands) {
        for (CommandData data : commands)
            this.commands.add(data);
    }

    @NotNull
    public List<CommandData> getCommands() {
        return commands;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommandListData that = (CommandListData) o;
        return commands.equals(that.commands);
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
    public boolean isEmpty() {
        return commands.isEmpty();
    }

    @Override
    public CommandListData randomize(MMOItemBuilder builder) {
        return new CommandListData(new ArrayList<>(commands));
    }
}