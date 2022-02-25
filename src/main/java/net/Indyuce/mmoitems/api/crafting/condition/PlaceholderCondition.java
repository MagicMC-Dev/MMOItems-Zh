package net.Indyuce.mmoitems.api.crafting.condition;

import io.lumine.mythic.lib.api.MMOLineConfig;
import me.clip.placeholderapi.PlaceholderAPI;
import net.Indyuce.mmoitems.api.player.PlayerData;

public class PlaceholderCondition extends GenericCondition {
    private final String value, placeholder, comparator, compareTo;

    public PlaceholderCondition(MMOLineConfig config) {
        super("placeholder", config);

        config.validate("placeholder");
        value = config.getString("placeholder");
        String[] array = value.split("~");
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
            case "<+":
                return Double.valueOf(placeholders) <= Double.valueOf(compareTo);
            case ">":
                return Double.valueOf(placeholders) > Double.valueOf(compareTo);
            case ">+":
                return Double.valueOf(placeholders) >= Double.valueOf(compareTo);
            case "++":
            case "+":
                return Double.valueOf(placeholders) == Double.valueOf(compareTo);
            case "!+":
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
