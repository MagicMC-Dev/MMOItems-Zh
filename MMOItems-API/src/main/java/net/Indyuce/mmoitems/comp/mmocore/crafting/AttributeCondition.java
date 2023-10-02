package net.Indyuce.mmoitems.comp.mmocore.crafting;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttribute;
import net.Indyuce.mmoitems.api.crafting.condition.Condition;
import org.apache.commons.lang.Validate;

public class AttributeCondition extends Condition {
    private final PlayerAttribute attribute;
    private final int points;

    public AttributeCondition(MMOLineConfig config) {
        super("attribute");

        config.validate("attribute", "points");

        points = config.getInt("points");

        String id = config.getString("attribute").toLowerCase().replace("_", "-");
        Validate.isTrue(MMOCore.plugin.attributeManager.has(id), "Could not find attribute " + id);
        attribute = MMOCore.plugin.attributeManager.get(id);
    }

    @Override
    public String formatDisplay(String string) {
        return string.replace("#level#", "" + points).replace("#attribute#", attribute.getName());
    }

    @Override
    public boolean isMet(net.Indyuce.mmoitems.api.player.PlayerData data) {
        return PlayerData.get(data.getPlayer()).getAttributes().getAttribute(attribute) >= points;
    }

    @Override
    public void whenCrafting(net.Indyuce.mmoitems.api.player.PlayerData data) {
    }
}
