package net.Indyuce.mmoitems.util;

import io.lumine.mythic.lib.util.lang3.Validate;
import org.jetbrains.annotations.NotNull;

/**
 * This class unsures that the {@link #build()}
 * method is called at most once time.
 *
 * @param <B> Type of object built
 */
public abstract class Buildable<B> {
    private boolean lock = true;

    protected abstract B whenBuilt();

    public boolean isBuilt() {
        return !lock;
    }

    @NotNull
    public B build() {
        Validate.isTrue(lock, "Has already been built");
        lock = false;
        return whenBuilt();
    }
}
