package net.Indyuce.mmoitems.api.crafting.condition;

import io.lumine.mythic.lib.api.MMOLineConfig;

public abstract class GenericCondition extends Condition {

    /**
     * Permissions are super ugly to display so MI uses a string instead.
     * This way 'Only for Mages' is used instead of 'class.mage'
     * <p>
     * One string can also replace multiple permissions.
     * 'Magic Classes Only' instead of 'class.mage' and 'class.apprentice'
     */
    public final String display;

    public GenericCondition(String id, MMOLineConfig config) {
        super(id);

        this.display = config.getString("display", "<No display chosen>");
    }

    @Override
    public String formatDisplay(String string) {
        return string.replace("#display#", display);
    }
}
