package net.Indyuce.mmoitems.api.interaction.weapon.untargeted;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import io.lumine.mythic.lib.util.CustomProjectile;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Crossbow extends UntargetedWeapon {
    public Crossbow(Player player, NBTItem item) {
        super(player, item, UntargetedWeaponType.RIGHT_CLICK);
    }

    @Override
    public boolean canAttack(EquipmentSlot slot) {
        return player.getGameMode() == GameMode.CREATIVE || getPlayer().getInventory().containsAtLeast(new ItemStack(Material.ARROW), 1);
    }

    @Override
    public void applyAttackEffect(PlayerMetadata stats, EquipmentSlot slot) {

        // Consume arrow
        if (getPlayer().getGameMode() != GameMode.CREATIVE)
            getPlayer().getInventory().removeItem(new ItemStack(Material.ARROW));

        final Arrow arrow = getPlayer().launchProjectile(Arrow.class);
        arrow.setVelocity(getPlayer().getEyeLocation().getDirection().multiply(3 * requireNonZero(getNBTItem().getStat(ItemStats.ARROW_VELOCITY.getId()), 1)));
        getPlayer().setVelocity(getPlayer().getVelocity().setX(0).setZ(0));
        getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ENTITY_ARROW_SHOOT, 1, 1);

        // Register custom projectile
        MMOItems.plugin.getEntities().registerCustomProjectile(getNBTItem(), stats, arrow, 1);

        // Trigger abilities
        stats.getData().triggerSkills(TriggerType.SHOOT_BOW, slot, arrow);
        new CustomProjectile(stats.getData(), CustomProjectile.ProjectileType.ARROW, arrow, slot);
    }
}
