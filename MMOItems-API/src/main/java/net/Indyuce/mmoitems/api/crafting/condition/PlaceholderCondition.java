package net.Indyuce.mmoitems.api.crafting.condition;

import io.lumine.mythic.lib.api.MMOLineConfig;
import me.clip.placeholderapi.PlaceholderAPI;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.apache.commons.lang.Validate;

import java.util.logging.Level;

public class PlaceholderCondition extends GenericCondition {
    private final String expression1, comparator, expression2;

    public PlaceholderCondition(MMOLineConfig config) {
        super("placeholder", config);

        config.validateKeys("placeholder");
        String[] array = config.getString("placeholder").split("~");
        Validate.isTrue(array.length == 3, "请务必使用两次~");
        expression1 = array[0];
        comparator = array[1];
        expression2 = array[2];
    }

    private static final double EQUALITY_THRESHOLD = Math.pow(10, -5);

    @Override
    public boolean isMet(PlayerData data) {
        final String unparsed1 = PlaceholderAPI.setPlaceholders(data.getPlayer(), expression1);
        final String unparsed2 = PlaceholderAPI.setPlaceholders(data.getPlayer(), expression2);
        try {
            switch (comparator) {
                case "<":
                    return Double.parseDouble(unparsed1) < Double.parseDouble(unparsed2);
                case "<=":
                    return Double.parseDouble(unparsed1) <= Double.parseDouble(unparsed2);
                case ">":
                    return Double.parseDouble(unparsed1) > Double.parseDouble(unparsed2);
                case ">=":
                    return Double.parseDouble(unparsed1) >= Double.parseDouble(unparsed2);
                case "==":
                case "=":
                    return Math.abs(Double.parseDouble(unparsed1) - Double.parseDouble(unparsed2)) <= EQUALITY_THRESHOLD;
                case "!=":
                    return Math.abs(Double.parseDouble(unparsed1) - Double.parseDouble(unparsed2)) > EQUALITY_THRESHOLD;
                case "equals":
                    return unparsed1.equals(unparsed2);
                default:
                    throw new RuntimeException("比较器无法识别");
            }
        } catch (RuntimeException exception) {
            MMOItems.plugin.getLogger().log(Level.WARNING, "无法计算占位符条件表达式 " + exception.getMessage());
            return false;
        }
    }

    @Override
    public void whenCrafting(PlayerData data) {
    }
}
