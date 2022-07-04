package net.Indyuce.mmoitems.comp.mmocore.load;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.dispenser.ExperienceDispenser;
import net.Indyuce.mmocore.experience.source.type.SpecificExperienceSource;
import net.Indyuce.mmocore.manager.profession.ExperienceSourceManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockCookEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class SmeltMMOItemExperienceSource extends SpecificExperienceSource<NBTItem> {
    private final String type, id;

    public SmeltMMOItemExperienceSource(ExperienceDispenser dispenser, MMOLineConfig config) {
        super(dispenser, config);

        config.validate("type", "id");
        type = config.getString("type").replace("-", "_").replace(" ", "_").toUpperCase();
        id = config.getString("id").replace("-", "_").replace(" ", "_").toUpperCase();
    }

    @Override
    public ExperienceSourceManager<SmeltMMOItemExperienceSource> newManager() {
        return new ExperienceSourceManager<SmeltMMOItemExperienceSource>() {

            @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
            public void a(BlockCookEvent event) {
                Optional<Player> player = getNearbyPlayer(event.getBlock().getLocation());
                if (!player.isPresent())
                    return;

                ItemStack caught = event.getResult();
                NBTItem nbt = MythicLib.plugin.getVersion().getWrapper().getNBTItem(caught);
                if (!nbt.hasType())
                    return;

                PlayerData data = PlayerData.get(player.get());
                for (SmeltMMOItemExperienceSource source : getSources())
                    if (source.matches(data, nbt))
                        source.giveExperience(data, 1, event.getBlock().getLocation().add(.5, 1, .5));
            }
        };
    }

    private Optional<Player> getNearbyPlayer(Location loc) {
        return loc.getWorld().getPlayers().stream().filter(player -> player.getLocation().distanceSquared(loc) < 100).findAny();
    }

    @Override
    public boolean matchesParameter(PlayerData player, NBTItem obj) {
        return obj.getString("MMOITEMS_ITEM_TYPE").equals(type) && obj.getString("MMOITEMS_ITEM_ID").equals(id);
    }
}
