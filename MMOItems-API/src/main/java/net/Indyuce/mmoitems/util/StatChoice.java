package net.Indyuce.mmoitems.util;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class StatChoice {
    private final String id, hint;

    public StatChoice(String id) {
        this(id, "- No Hint Available - ");
    }

    public StatChoice(String id, @Nullable String hint) {
        this.id = id;
        this.hint = hint;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public String getHint() {
        return hint;
    }

    @Override
    public String toString() {
        return getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatChoice choice = (StatChoice) o;
        return id.equals(choice.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
