package net.Indyuce.mmoitems.comp.mythicmobs.skill;

import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.adapters.AbstractLocation;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.mobs.GenericCaster;
import io.lumine.xikage.mythicmobs.skills.Skill;
import io.lumine.xikage.mythicmobs.skills.SkillCaster;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.skills.SkillTrigger;
import net.Indyuce.mmoitems.ability.Ability;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;

import java.util.HashSet;
import java.util.Optional;

public class MythicMobsAbility extends Ability<MythicMobsAbilityMetadata> {
    private Skill skill;

    public MythicMobsAbility(String id, FileConfiguration config) {
        super(id, config.getString("name"));

        String skillName = config.getString("mythicmobs-skill-id");
        Validate.notNull(skillName, "Could not find MM skill name");

        Optional<io.lumine.xikage.mythicmobs.skills.Skill> opt = MythicMobs.inst().getSkillManager().getSkill(skillName);
        Validate.isTrue(opt.isPresent(), "Could not find MM skill with name '" + skillName + "'");
        skill = opt.get();

        addModifier("cooldown", 10);
        addModifier("mana", 0);
        addModifier("stamina", 0);

        for (String mod : config.getKeys(false))
            if (!mod.equals("name") && !mod.equals("mythicmobs-skill-id") && !mod.equals("self-only"))
                addModifier(mod.toLowerCase().replace("_", "-").replace(" ", "-"), config.getInt(mod));
    }

    public String getInternalName() {
        return skill.getInternalName();
    }

    public void setSkill(Skill skill) {
        this.skill = skill;
    }

    @Override
    public void whenCast(AttackMetadata attackMeta, MythicMobsAbilityMetadata ability) {
        skill.execute(ability.getSkillMetadata());
    }

    @Override
    public MythicMobsAbilityMetadata canBeCast(AttackMetadata attackMeta, LivingEntity target, AbilityData data) {

        // TODO what's the difference between trigger and caster.
        AbstractEntity trigger = BukkitAdapter.adapt(attackMeta.getPlayer());
        SkillCaster caster = new GenericCaster(trigger);

        HashSet<AbstractEntity> targetEntities = new HashSet<>();
        HashSet<AbstractLocation> targetLocations = new HashSet<>();

        targetEntities.add(BukkitAdapter.adapt(target));

        SkillMetadata skillMeta = new SkillMetadata(SkillTrigger.CAST, caster, trigger, BukkitAdapter.adapt(attackMeta.getPlayer().getEyeLocation()), targetEntities, targetLocations, 1);

        // Stats are cached inside a variable
        skillMeta.getVariables().putObject("MMOStatMap", attackMeta.getStats());
        skillMeta.getVariables().putObject("MMOSkill", data);

        return new MythicMobsAbilityMetadata(data, skill, skillMeta);
    }
}
