package net.Indyuce.mmoitems.listener;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.utils.Events;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.manager.Reloadable;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class EquipListener implements Reloadable, Listener {
    public EquipListener(){
        this.reload();
    }
    @Override
    public void reload() {
        if(MMOItems.plugin.getConfig().getBoolean("auto-equip-feature", true)) {
            Events.subscribe(PlayerInteractEvent.class).handler(event -> {
                /*
                 *    We only care about listening for right click and shift right clicks.
                 */
                if (!(event.getAction().equals(Action.RIGHT_CLICK_AIR))) {
                    return;
                }
                if(!(event.getHand().equals(EquipmentSlot.HAND))){
                    return;
                }

                if (event.getItem() == null || event.getPlayer() == null) {
                    return;
                }

                if (event.getItem().getType().toString().toLowerCase().contains("boots")
                        || event.getItem().getType().toString().toLowerCase().contains("leggings")
                        || event.getItem().getType().toString().toLowerCase().contains("chestplate")
                        || event.getItem().getType().toString().toLowerCase().contains("helmet")) {

                    NBTItem nbtItem = NBTItem.get(event.getItem());

                    Integer priority = nbtItem.getInteger("MMOITEMS_EQUIP_PRIORITY");

                    if (event.getItem().getType().toString().toLowerCase().contains("helmet")) {
                        NBTItem helmet = NBTItem.get(event.getPlayer().getInventory().getHelmet());
                        Integer helmPriority = helmet.getInteger("MMOITEMS_EQUIP_PRIORITY");
                        if (priority >= helmPriority) {
                            ItemStack h = event.getPlayer().getInventory().getHelmet();
                            event.getPlayer().getInventory().setHelmet(event.getItem());
                            event.getPlayer().getInventory().setItemInMainHand(h);
                        }
                    } else if (event.getItem().getType().toString().toLowerCase().contains("chestplate")) {
                        NBTItem chestplate = NBTItem.get(event.getPlayer().getInventory().getChestplate());
                        Integer chestPriority = chestplate.getInteger("MMOITEMS_EQUIP_PRIORITY");
                        if (priority >= chestPriority) {
                            ItemStack c = event.getPlayer().getInventory().getChestplate();
                            event.getPlayer().getInventory().setChestplate(event.getItem());
                            event.getPlayer().getInventory().setItemInMainHand(c);
                        }
                    } else if (event.getItem().getType().toString().toLowerCase().contains("leggings")) {
                        NBTItem leggings = NBTItem.get(event.getPlayer().getInventory().getLeggings());
                        Integer legPriority = leggings.getInteger("MMOITEMS_EQUIP_PRIORITY");
                        if (priority >= legPriority) {
                            ItemStack l = event.getPlayer().getInventory().getLeggings();
                            event.getPlayer().getInventory().setLeggings(event.getItem());
                            event.getPlayer().getInventory().setItemInMainHand(l);
                        }
                    } else {
                        NBTItem boots = NBTItem.get(event.getPlayer().getInventory().getBoots());
                        Integer bootsPriority = boots.getInteger("MMOITEMS_EQUIP_PRIORITY");
                        if (priority >= bootsPriority) {
                            ItemStack b = event.getPlayer().getInventory().getBoots();
                            event.getPlayer().getInventory().setBoots(event.getItem());
                            event.getPlayer().getInventory().setItemInMainHand(b);
                        }
                    }

                }

            });
        }
    }
}
