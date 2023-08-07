package net.Indyuce.mmoitems.comp.mythicmobs.crafting;

import io.lumine.mythic.api.skills.Skill;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmoitems.api.crafting.trigger.Trigger;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MythicMobsSkillTrigger extends Trigger {
    private final Skill skill;

    public MythicMobsSkillTrigger(MMOLineConfig config) {
        super("mmskill");

        config.validate("id");
        String id = config.getString("id");
        Optional<Skill> opt = MythicBukkit.inst().getSkillManager().getSkill(id);
        Validate.isTrue(opt.isPresent(), "Could not find MM skill " + id);
        skill = opt.get();
    }

    @Override
    public void whenCrafting(PlayerData data) {
        if (!data.isOnline()) return;
        List<Entity> targets = new ArrayList<>();
        targets.add(data.getPlayer());
        MythicBukkit.inst().getAPIHelper().castSkill(data.getPlayer(), this.skill.getInternalName(), data.getPlayer(), data.getPlayer().getEyeLocation(), targets, null, 1);
    }
}
