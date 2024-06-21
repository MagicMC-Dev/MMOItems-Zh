package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.version.VMaterial;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.util.VersionDependant;

@VersionDependant(version = {1, 20, 5})
public class EntityInteractionRange extends DoubleStat {
    public EntityInteractionRange() {
        super("ENTITY_INTERACTION_RANGE", VMaterial.SPYGLASS.get(),
                "生物交互距离", new String[]{"破坏实体或与实体相互作用的距离。", "玩家默认在创造中的值为 5，", "在生存中为的值 4.5"});
    }
}
