package net.Indyuce.mmoitems.comp.denizen;

import com.denizenscript.denizencore.objects.ObjectTag;

public abstract class SimpleTag implements ObjectTag {

    // No idea what this is
    private String prefix;

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public ObjectTag setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }
}
