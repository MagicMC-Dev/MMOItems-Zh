package net.Indyuce.mmoitems.ability.list.vector;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.ability.VectorAbility;
import net.Indyuce.mmoitems.ability.metadata.VectorAbilityMetadata;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import net.Indyuce.mmoitems.api.util.TemporaryListener;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class Corrupted_Fangs extends VectorAbility implements Listener {
    public Corrupted_Fangs() {
        super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK,
                CastingMode.SHIFT_RIGHT_CLICK);

        addModifier("damage", 5);
        addModifier("cooldown", 12);
        addModifier("mana", 0);
        addModifier("stamina", 0);
        addModifier("fangs", 6);
    }

    @Override
    public void whenCast(ItemAttackMetadata attack, VectorAbilityMetadata ability) {
        attack.getDamager().getWorld().playSound(attack.getDamager().getLocation(), Sound.ENTITY_WITHER_SHOOT, 2, 2);
        new BukkitRunnable() {
            final Vector vec = ability.getTarget().setY(0).multiply(2);
            final Location loc = attack.getDamager().getLocation();
            final FangsHandler handler = new FangsHandler(attack, ability.getModifier("damage"));
            final double fangAmount = ability.getModifier("fangs");
            double ti = 0;

            public void run() {
                if (ti++ >= fangAmount) {
                    handler.close(3 * 20);
                    cancel();
                    return;
                }

                loc.add(vec);
                EvokerFangs evokerFangs = (EvokerFangs) attack.getDamager().getWorld().spawnEntity(loc, EntityType.EVOKER_FANGS);
                handler.entities.add(evokerFangs.getEntityId());
            }
        }.runTaskTimer(MMOItems.plugin, 0, 1);
    }

    public class FangsHandler extends TemporaryListener {
        private final Set<Integer> entities = new HashSet<>();
        private final ItemAttackMetadata attackMeta;
        private final double damage;

        public FangsHandler(ItemAttackMetadata attackMeta, double damage) {
            super(EntityDamageByEntityEvent.getHandlerList());

            this.attackMeta = attackMeta;
            this.damage = damage;
        }

        @EventHandler
        public void a(EntityDamageByEntityEvent event) {
            if (event.getDamager() instanceof EvokerFangs && entities.contains(event.getDamager().getEntityId())) {
                event.setCancelled(true);

                if (MMOUtils.canDamage(attackMeta.getDamager(), event.getEntity()))
                    attackMeta.damage((LivingEntity) event.getEntity());
            }
        }
    }
}
