package net.Indyuce.mmoitems.util;

import org.apache.commons.lang.Validate;
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

    public void validateNotBuilt() {
        Validate.isTrue(lock, "Has already been built");
    }

    public boolean isBuilt() {
        return !lock;
    }

    @NotNull
    public B build() {
        validateNotBuilt();
        lock = false;
        return whenBuilt();
    }
}
