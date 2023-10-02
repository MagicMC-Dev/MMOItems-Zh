package net.Indyuce.mmoitems.listener;

import fr.phoenixdevt.profiles.event.ProfileSelectEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ProfileSupportListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void a(ProfileSelectEvent event) {
        ItemListener.updateInventory(event.getPlayer());
    }
}
