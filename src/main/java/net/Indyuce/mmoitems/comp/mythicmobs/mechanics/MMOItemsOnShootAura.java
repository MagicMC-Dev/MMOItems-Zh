package net.Indyuce.mmoitems.comp.mythicmobs.mechanics;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.mobs.GenericCaster;
import io.lumine.mythic.api.skills.*;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.auras.Aura;
import io.lumine.mythic.bukkit.utils.Events;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.event.item.SpecialWeaponAttackEvent;
import net.Indyuce.mmoitems.api.interaction.weapon.Gauntlet;
import net.Indyuce.mmoitems.api.interaction.weapon.untargeted.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

/**
 * Sure there is the 'onShoot' aura for bows, but what about
 * musket and crossbow and lute and...?
 * <p>
 * This Aura will cover all of those.
 *
 * @author Gunging
 */
public class MMOItemsOnShootAura extends Aura implements ITargetedEntitySkill {
    @NotNull
    PlaceholderString skillName;
    @NotNull
    String weaponTypes;
    @Nullable
    Skill metaskill;
    boolean cancelEvent;
    boolean forceAsPower;

    @NotNull
    final ArrayList<UseItemTypes> auraWeapons = new ArrayList<>();

    public MMOItemsOnShootAura(SkillExecutor manager, String skill, MythicLineConfig mlc) {
        super(manager, skill, mlc);
        skillName = mlc.getPlaceholderString(new String[]{"skill", "s", "ondamagedskill", "ondamaged", "od", "onhitskill", "onhit", "oh", "meta", "m", "mechanics", "$", "()"}, "skill not found");
        weaponTypes = mlc.getString(new String[]{"weapons", "weapon", "w"}, "MUSKET");
        metaskill = GetSkill(skillName.get());
        cancelEvent = mlc.getBoolean(new String[]{"cancelevent", "ce"}, false);

        // Parse weapon types
        ArrayList<String> weaponTypesUnparsed = new ArrayList<>();
        if (weaponTypes.contains(",")) {
            weaponTypesUnparsed.addAll(Arrays.asList(weaponTypes.split(",")));
        } else {
            weaponTypesUnparsed.add(weaponTypes);
        }

        for (String weapon : weaponTypesUnparsed) {
            // Try to get
            try {

                // Parse
                UseItemTypes weap = UseItemTypes.valueOf(weapon.toUpperCase());

                // Yes
                auraWeapons.add(weap);

            } catch (IllegalArgumentException ignored) {
            }
        }

        // Attempt to fix meta skill
        if (metaskill == null) {
            //MM//OotilityCeption.Log("\u00a7c--->> \u00a7eMeta Skill Failure \u00a7c<<---");

            // Try again i guess?
            (new BukkitRunnable() {
                public void run() {

                    // Run Async
                    metaskill = GetSkill(skillName.get());

                }
            }).runTaskLater(MMOItems.plugin, 1L);
        }
    }

    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        // Find caster
        SkillCaster caster;

        // Will be caster of the skill, as a mythicmob
        if (MythicBukkit.inst().getMobManager().isActiveMob(target)) {
            //SOM//OotilityCeption.Log("\u00a73  * \u00a77Target as ActiveMob");

            // Just pull the mythicmob
            caster = MythicBukkit.inst().getMobManager().getMythicMobInstance(target);

            // If its a player or some other non-mythicmob
        } else {
            //SOM//OotilityCeption.Log("\u00a73  * \u00a77Target as Non MM");

            // I guess make a new caster out of them
            caster = new GenericCaster(target);
        }

        new MMOItemsOnShootAura.Tracker(caster, data, target);
        return SkillResult.SUCCESS;
    }

    private class Tracker extends Aura.AuraTracker implements IParentSkill, Runnable {
        public Tracker(SkillCaster caster, SkillMetadata data, AbstractEntity entity) {
            super(caster, entity, data);
            this.start();
        }

        public void auraStart() {
            this.registerAuraComponent(Events.subscribe(SpecialWeaponAttackEvent.class).filter((event) -> {

                //SOM//OotilityCeption.Log("\u00a7cStep 3 \u00a77Subscribe Run: " + getName(event.getEntity()) + "\u00a77 vs " + getName(this.entity.get()) + "\u00a78 ~\u00a7e " + event.getEntity().getUniqueId().equals(this.entity.get().getUniqueId()));

                // Player is the one who has the aura applied, right?
                if (!event.getPlayer().getUniqueId().equals(this.entity.get().getUniqueId())) {
                    return false;
                }

                // All custom weapons fire it if none specified.
                if (auraWeapons.size() == 0) {
                    return true;
                }

                // Okay go through all weapon types, must match one
                for (UseItemTypes weap : auraWeapons) {
                    if (weap.getInst().isInstance(event.getWeapon())) {
                        return true;
                    }
                }

                // None matched
                return false;

            }).handler((event) -> {

                // Clone metadata
                SkillMetadata meta = this.skillMetadata.deepClone();

                // Refresh
                if (metaskill == null) {
                    metaskill = GetSkill(skillName.get(meta, meta.getCaster().getEntity()));
                }

                // Target obviously the projectile
                AbstractEntity target = BukkitAdapter.adapt(event.getTarget());
                meta.setTrigger(target);

                //SOM//OotilityCeption.Log("\u00a7cStep 4 \u00a77Aura Run:\u00a7d " + logSkillData(meta) + "\u00a7b " + metaskill.getInternalName());
                if (this.executeAuraSkill(Optional.ofNullable(metaskill), meta)) {

                    this.consumeCharge();

                    if (cancelEvent) {
                        event.setCancelled(true);
                    }
                }

            }));
            this.executeAuraSkill(MMOItemsOnShootAura.this.onStartSkill, this.skillMetadata);
        }
    }

    @Nullable
    public static Skill GetSkill(String skillName) {

        if (SkillExists(skillName)) {

            Optional<Skill> mSkillFk = MythicBukkit.inst().getSkillManager().getSkill(skillName);
            if (mSkillFk == null) {
                return null;
            }
            if (mSkillFk.isPresent()) {
                return mSkillFk.get();
            }
        }

        return null;
    }

    public static boolean SkillExists(String skillName) {
        // If null no
        if (skillName == null) {
            return false;
        }

        Optional<Skill> mSkillFk = MythicBukkit.inst().getSkillManager().getSkill(skillName);

        // Is there a skill of that name?
        if (mSkillFk.isPresent()) {

            try {
                // Ok then retrieve the skill
                Skill mSkill = (Skill) mSkillFk.get();

                // Success
                return true;

            } catch (Exception e) {

                // RIP
                return false;
            }

            // The skill was not found
        } else {

            // False means the skill does not exist.
            return false;
        }
    }

    enum UseItemTypes {
        CROSSBOW(Crossbow.class),
        GAUNTLET(Gauntlet.class),
        LUTE(Lute.class),
        MUSKET(Musket.class),
        STAFF(Staff.class),
        WHIP(Whip.class);

        /**
         * @return Class to use InstanceOf and identify a weapon.
         */
        @NotNull
        public Class getInst() {
            return inst;
        }

        @NotNull
        final Class inst;

        UseItemTypes(@NotNull Class inst) {
            this.inst = inst;
        }
    }
}
