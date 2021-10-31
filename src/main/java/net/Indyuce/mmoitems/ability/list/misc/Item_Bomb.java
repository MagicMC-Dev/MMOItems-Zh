package net.Indyuce.mmoitems.ability.list.misc;

import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.version.VersionSound;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.ability.ItemAbility;
import net.Indyuce.mmoitems.ability.metadata.ItemAbilityMetadata;
import io.lumine.mythic.lib.damage.AttackMetadata;
import net.Indyuce.mmoitems.api.util.NoClipItem;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class Item_Bomb extends ItemAbility implements Listener {
    public Item_Bomb() {
        super();

        addModifier("damage", 7);
        addModifier("radius", 6);
        addModifier("slow-duration", 4);
        addModifier("slow-amplifier", 1);
        addModifier("cooldown", 15);
        addModifier("mana", 0);
        addModifier("stamina", 0);
    }

    @Override
    public void whenCast(AttackMetadata attack, ItemAbilityMetadata ability) {
        ItemStack itemStack = ability.getItem();
        final NoClipItem item = new NoClipItem(attack.getPlayer().getLocation().add(0, 1.2, 0), itemStack);
        item.getEntity().setVelocity(ability.getTarget().multiply(1.3));
        attack.getPlayer().getWorld().playSound(attack.getPlayer().getLocation(), Sound.ENTITY_SNOWBALL_THROW, 2, 0);

        new BukkitRunnable() {
            int j = 0;

            public void run() {
                if (j++ > 40) {
                    double radius = ability.getModifier("radius");
                    double damage = ability.getModifier("damage");
                    double slowDuration = ability.getModifier("slow-duration");
                    double slowAmplifier = ability.getModifier("slow-amplifier");

                    for (Entity entity : item.getEntity().getNearbyEntities(radius, radius, radius))
                        if (MMOUtils.canTarget(attack.getPlayer(), entity)) {
                            new AttackMetadata(new DamageMetadata(damage, DamageType.SKILL, DamageType.PHYSICAL), attack.getStats()).damage((LivingEntity) entity);
                            ((LivingEntity) entity).removePotionEffect(PotionEffectType.SLOW);
                            ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (slowDuration * 20), (int) slowAmplifier));
                        }

                    item.getEntity().getWorld().spawnParticle(Particle.EXPLOSION_LARGE, item.getEntity().getLocation(), 24, 2, 2, 2, 0);
                    item.getEntity().getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, item.getEntity().getLocation(), 48, 0, 0, 0, .2);
                    item.getEntity().getWorld().playSound(item.getEntity().getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 3, 0);

                    item.close();
                    cancel();
                    return;
                }

				item.getEntity().getWorld().spawnParticle(Particle.SMOKE_LARGE, item.getEntity().getLocation().add(0, .2, 0), 0);
				item.getEntity().getWorld().spawnParticle(Particle.FIREWORKS_SPARK, item.getEntity().getLocation().add(0, .2, 0), 1, 0, 0, 0, .1);
				item.getEntity().getWorld().playSound(item.getEntity().getLocation(), VersionSound.BLOCK_NOTE_BLOCK_HAT.toSound(), 2, (float) (.5 + (j / 40. * 1.5)));
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}
