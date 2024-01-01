package net.Indyuce.mmoitems.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Version string is MAJOR.MINOR.PATCH
 * <p>
 * This annotation indicates the LOWEST VERSION at which
 * the given feature is available. Usually, it's the
 * version where some non-backwards compatible feature was
 * implemented into Minecraft or Spigot.
 *
 * @author Jules
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface VersionDependant {

    public int major() default 1;

    public int minor();

    public int patch() default 0;
}
