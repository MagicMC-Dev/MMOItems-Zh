package net.Indyuce.mmoitems.command.mmoitems;

import java.util.HashSet;
import java.util.Set;

public class GenerateCommandHandler {
    private final Set<String> arguments = new HashSet<>();

    public GenerateCommandHandler(String... args) {
        for (String arg : args)
            arguments.add(arg.toLowerCase());
    }

    public boolean hasArgument(String key) {
        for (String argument : arguments)
            if (argument.startsWith("-" + key))
                return true;
        return false;
    }

    public String getValue(String key) {
        for (String argument : arguments)
            if (argument.startsWith("-" + key + ":"))
                return argument.substring(key.length() + 2);

        throw new IllegalArgumentException("Command has no argument '" + key + "'");
    }
}
