package net.Indyuce.mmoitems.skill.custom.condition;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.script.condition.Condition;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import net.Indyuce.mmoitems.api.player.PlayerData;

import java.util.Arrays;
import java.util.List;

@Deprecated
public class ClassCondition extends Condition {
    private final List<String> classes;

    public ClassCondition(ConfigObject config) {
        super(config);

        config.validateKeys("list");
        classes = Arrays.asList(config.getString("list").split(","));
    }

    @Override
    public boolean isMet(SkillMetadata skillMetadata) {
        PlayerData playerData = PlayerData.get(skillMetadata.getCaster().getPlayer());
        return classes.contains(playerData.getRPG().getClassName());
    }
}
