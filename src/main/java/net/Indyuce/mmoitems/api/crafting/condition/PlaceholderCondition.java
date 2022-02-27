package net.Indyuce.mmoitems.api.crafting.condition;

import io.lumine.mythic.lib.api.MMOLineConfig;
import me.clip.placeholderapi.PlaceholderAPI;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.apache.commons.lang.Validate;

public class PlaceholderCondition extends GenericCondition {
    private final String placeholder, comparator, compareTo;

    public PlaceholderCondition(MMOLineConfig config) {
        super("placeholder", config);

        config.validate("placeholder");
        String[] array = config.getString("placeholder").split("~");
        Validate.isTrue(array.length == 3, "Please use exactly three times ~");
        placeholder = array[0];
        comparator = array[1];
        compareTo = array[2];
    }

    @Override
    public boolean isMet(PlayerData data) {
        String placeholders = PlaceholderAPI.setPlaceholders(data.getPlayer(), placeholder);
        switch (comparator) {
            case "<":
                return Double.valueOf(placeholders) < Double.valueOf(compareTo);
            case "<=":
                return Double.valueOf(placeholders) <= Double.valueOf(compareTo);
            case ">":
                return Double.valueOf(placeholders) > Double.valueOf(compareTo);
            case ">=":
                return Double.valueOf(placeholders) >= Double.valueOf(compareTo);
            case "==":
            case "=":
                return Double.valueOf(placeholders) == Double.valueOf(compareTo);
            case "!=":
                return Double.valueOf(placeholders) != Double.valueOf(compareTo);
            case "equals":
                return placeholders.equals(compareTo);
        }
        return false;
    }

    @Override
    public void whenCrafting(PlayerData data) {
    }
}
