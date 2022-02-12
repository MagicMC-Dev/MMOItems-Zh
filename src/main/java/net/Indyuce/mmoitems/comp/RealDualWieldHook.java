package net.Indyuce.mmoitems.comp;

import com.evill4mer.RealDualWield.Api.PlayerDamageEntityWithOffhandEvent;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.damage.MeleeAttackMetadata;
import io.lumine.mythic.lib.player.PlayerMetadata;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.TypeSet;
import net.Indyuce.mmoitems.api.interaction.weapon.Weapon;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class RealDualWieldHook implements Listener {
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void a(PlayerDamageEntityWithOffhandEvent event) {

        /*
         * Citizens and Sentinels NPC support; damage = 0 check to ignore safety
         * checks; check for entity attack
         */
        if (event.getDamage() == 0 || !(event.getEntity() instanceof LivingEntity) || event.getEntity().hasMetadata("NPC") || event.getPlayer().hasMetadata("NPC"))
            return;

        /*
         * Must apply attack conditions before apply any effects. the event must
         * be cancelled before anything is applied
         */
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.get(player);
        LivingEntity target = (LivingEntity) event.getEntity();
        NBTItem item = MythicLib.plugin.getVersion().getWrapper().getNBTItem(player.getInventory().getItemInMainHand());
        MeleeAttackMetadata attackMeta = null;

        if (item.hasType() && Type.get(item.getType()) != Type.BLOCK) {
            Weapon weapon = new Weapon(playerData, item);

            if (weapon.getMMOItem().getType().getItemSet() == TypeSet.RANGE) {
                event.setCancelled(true);
                return;
            }

            if (!weapon.checkItemRequirements()) {
                event.setCancelled(true);
                return;
            }

            if (!weapon.handleTargetedAttack(attackMeta = getAttack(playerData, event), target)) {
                event.setCancelled(true);
                return;
            }
        }

        // Cast on-hit abilities and add the extra damage to the damage event
        new ItemAttackMetadata(attackMeta == null ? attackMeta = getAttack(playerData, event) : attackMeta).applyEffects(item, target);

        // Finally update Bukkit event
        event.setDamage(attackMeta.getDamage().getDamage());
    }

    private boolean isAir(@Nullable ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }

    private MeleeAttackMetadata getAttack(PlayerData playerData, PlayerDamageEntityWithOffhandEvent event) {
        PlayerMetadata cachedStatMap = playerData.getMMOPlayerData().getStatMap().cache(EquipmentSlot.OFF_HAND);
        DamageType[] damageTypes = isAir(playerData.getPlayer().getInventory().getItemInOffHand()) ? new DamageType[]{DamageType.UNARMED, DamageType.PHYSICAL} : new DamageType[]{DamageType.WEAPON, DamageType.PHYSICAL};
        return new MeleeAttackMetadata(new DamageMetadata(event.getDamage(), damageTypes), cachedStatMap);
    }
}
