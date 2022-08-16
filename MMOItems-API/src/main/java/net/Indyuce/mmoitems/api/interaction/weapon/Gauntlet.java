package net.Indyuce.mmoitems.api.interaction.weapon;

import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.player.PlayerData.CooldownType;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Gauntlet extends Weapon {
    public Gauntlet(Player player, NBTItem item) {
        super(player, item);
    }

    public void specialAttack(LivingEntity target) {
        if (!MMOItems.plugin.getConfig().getBoolean("item-ability.gauntlet.enabled"))
            return;

        if (!checkWeaponCosts(CooldownType.SPECIAL_ATTACK))
            return;

        applyWeaponCosts(MMOItems.plugin.getConfig().getDouble("item-ability.gauntlet.cooldown"), CooldownType.SPECIAL_ATTACK);
        target.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, target.getLocation().add(0, 1, 0), 0);
        target.getWorld().playSound(target.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 0);
        target.removePotionEffect(PotionEffectType.BLINDNESS);
        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 1));
        target.setVelocity(getPlayer().getEyeLocation().getDirection().setY(0).normalize().setY(.8));
        target.setVelocity(getPlayer().getEyeLocation().getDirection().setY(0).normalize().multiply(2).setY(.3));
    }
}
