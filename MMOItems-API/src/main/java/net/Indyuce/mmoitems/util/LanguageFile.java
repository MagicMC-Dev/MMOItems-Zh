package net.Indyuce.mmoitems.util;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

/**
 * Util class to make sure language files are up to date
 *
 * @author jules
 */
public class LanguageFile extends ConfigFile {
    private boolean change;

    public LanguageFile(String name) {
        super(MMOItems.plugin, "/language", name);
    }

    @NotNull
    public String computeTranslation(String path, Provider<String> defaultTranslation) {
        @Nullable String found = getConfig().getString(path);
        if (found == null) {
            change = true;
            getConfig().set(path, found = defaultTranslation.get());
            MMOItems.plugin.getLogger().log(Level.INFO, "Could not find translation for '" + path + "', generating it");
        }

        return found;
    }

    /**
     * Only saves if changes have been detected.
     */
    @Override
    public void save() {
        if (change)
            super.save();
    }
}
