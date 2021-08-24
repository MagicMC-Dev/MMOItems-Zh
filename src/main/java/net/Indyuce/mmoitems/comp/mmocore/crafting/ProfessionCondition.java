package net.Indyuce.mmoitems.comp.mmocore.crafting;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.Profession;
import net.Indyuce.mmoitems.api.crafting.condition.Condition;
import org.apache.commons.lang.Validate;

public class ProfessionCondition extends Condition {
    private final Profession profession;
    private final int level;

    public ProfessionCondition(MMOLineConfig config) {
        super("profession");

        config.validate("profession", "level");

        level = config.getInt("level");

        String id = config.getString("profession").toLowerCase().replace("_", "-");
        Validate.isTrue(MMOCore.plugin.professionManager.has(id), "Could not find profession " + id);
        profession = MMOCore.plugin.professionManager.get(id);
    }


    @Override
    public String formatDisplay(String string) {
        return string.replace("#level#", "" + level).replace("#profession#", profession.getName());
    }

    @Override
    public boolean isMet(net.Indyuce.mmoitems.api.player.PlayerData data) {
        return PlayerData.get(data.getUniqueId()).getCollectionSkills().getLevel(profession) >= level;
    }

    @Override
    public void whenCrafting(net.Indyuce.mmoitems.api.player.PlayerData data) {
    }
}
