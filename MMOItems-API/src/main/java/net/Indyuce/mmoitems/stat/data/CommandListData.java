package net.Indyuce.mmoitems.stat.data;

import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CommandListData implements StatData, Mergeable<CommandListData>, RandomStatData<CommandListData> {
    @NotNull
    private final List<CommandData> commands = new ArrayList<>();

    public CommandListData(@NotNull List<CommandData> commands) {
        add(commands);
    }

    public CommandListData(CommandData... commands) {
        add(commands);
    }

    public void add(CommandData... commands) {
        for (CommandData data : commands) this.commands.add(data);
    }

    public void add(Collection<CommandData> commands) {
        this.commands.addAll(commands);
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
    public void mergeWith(CommandListData data) {
        commands.addAll(data.commands);
    }

    @NotNull
    @Override
    public CommandListData clone() {
        return new CommandListData(commands);
    }

    @Override
    public boolean isEmpty() {
        return commands.isEmpty();
    }

    @Override
    public CommandListData randomize(MMOItemBuilder builder) {
        return clone();
    }
}