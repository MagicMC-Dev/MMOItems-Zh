package net.Indyuce.mmoitems.ability.list.vector;

import io.lumine.mythic.lib.api.util.TemporaryListener;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.ability.VectorAbility;
import net.Indyuce.mmoitems.ability.metadata.VectorAbilityMetadata;
import io.lumine.mythic.lib.damage.AttackMetadata;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class Corrupted_Fangs extends VectorAbility {
    public Corrupted_Fangs() {
        super();

        addModifier("damage", 5);
        addModifier("cooldown", 12);
        addModifier("mana", 0);
        addModifier("stamina", 0);
        addModifier("fangs", 6);
    }

    @Override
    public void whenCast(AttackMetadata attack, VectorAbilityMetadata ability) {
        attack.getPlayer().getWorld().playSound(attack.getPlayer().getLocation(), Sound.ENTITY_WITHER_SHOOT, 2, 2);
        new BukkitRunnable() {
            final Vector vec = ability.getTarget().setY(0).multiply(2);
            final Location loc = attack.getPlayer().getLocation();
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
                EvokerFangs evokerFangs = (EvokerFangs) attack.getPlayer().getWorld().spawnEntity(loc, EntityType.EVOKER_FANGS);
                handler.entities.add(evokerFangs.getEntityId());
            }
        }.runTaskTimer(MMOItems.plugin, 0, 1);
    }

    public class FangsHandler extends TemporaryListener {
        private final Set<Integer> entities = new HashSet<>();
        private final AttackMetadata attackMeta;
        private final double damage;

        public FangsHandler(AttackMetadata attackMeta, double damage) {
            super(EntityDamageByEntityEvent.getHandlerList());

            this.attackMeta = attackMeta;
            this.damage = damage;
        }

        @EventHandler
        public void a(EntityDamageByEntityEvent event) {
            if (event.getDamager() instanceof EvokerFangs && entities.contains(event.getDamager().getEntityId())) {
                event.setCancelled(true);

                if (MMOUtils.canTarget(attackMeta.getPlayer(), event.getEntity()))
                    attackMeta.damage((LivingEntity) event.getEntity());
            }
        }

        @Override
        public void whenClosed() {
            // Nothing
        }
    }
}
