package net.Indyuce.mmoitems.ability.list.misc;

import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.ability.ItemAbility;
import net.Indyuce.mmoitems.ability.metadata.ItemAbilityMetadata;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import net.Indyuce.mmoitems.api.util.NoClipItem;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class Item_Throw extends ItemAbility implements Listener {
    public Item_Throw() {
        super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

        addModifier("damage", 6);
        addModifier("force", 1);
        addModifier("cooldown", 10);
        addModifier("mana", 0);
        addModifier("stamina", 0);
    }

    @Override
    public void whenCast(ItemAttackMetadata attack, ItemAbilityMetadata ability) {
        ItemStack itemStack = ability.getItem();
        /*boolean hasAbility = false;

        for (JsonElement entry : MythicLib.plugin.getJson().parse(nbtItem.getString("MMOITEMS_ABILITY"), JsonArray.class)) {
            if (!entry.isJsonObject())
				continue;

			JsonObject object = entry.getAsJsonObject();
            if (object.get("Id").getAsString().equalsIgnoreCase(getID())) {
                hasAbility = true;
                break;
            }
        }

        if (!hasAbility)
            return;*/

        final NoClipItem item = new NoClipItem(attack.getDamager().getLocation().add(0, 1.2, 0), itemStack);
        item.getEntity().setVelocity(ability.getTarget().multiply(1.5 * ability.getModifier("force")));
        attack.getDamager().getWorld().playSound(attack.getDamager().getLocation(), Sound.ENTITY_SNOWBALL_THROW, 1, 0);
        new BukkitRunnable() {
            double ti = 0;

            public void run() {
                ti++;
                if (ti > 20 || item.getEntity().isDead()) {
                    item.close();
                    cancel();
                }

                item.getEntity().getWorld().spawnParticle(Particle.CRIT, item.getEntity().getLocation(), 0);
                for (Entity target : item.getEntity().getNearbyEntities(1, 1, 1))
                    if (MMOUtils.canTarget(attack.getDamager(), target)) {
                        new AttackMetadata(new DamageMetadata(ability.getModifier("damage"), DamageType.SKILL, DamageType.PHYSICAL, DamageType.PROJECTILE), attack.getStats()).damage((LivingEntity) target);
                        item.close();
                        cancel();
                    }
            }
        }.runTaskTimer(MMOItems.plugin, 0, 1);
    }
}
