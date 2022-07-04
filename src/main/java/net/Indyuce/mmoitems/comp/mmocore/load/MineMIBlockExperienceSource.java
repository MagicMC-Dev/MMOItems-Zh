package net.Indyuce.mmoitems.comp.mmocore.load;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.dispenser.ExperienceDispenser;
import net.Indyuce.mmocore.experience.source.type.SpecificExperienceSource;
import net.Indyuce.mmocore.manager.profession.ExperienceSourceManager;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.block.CustomBlock;
import org.bukkit.GameMode;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class MineMIBlockExperienceSource extends SpecificExperienceSource<Integer> {
    private final int id;
    private final boolean silkTouch;
    private final boolean playerPlaced;

    public MineMIBlockExperienceSource(ExperienceDispenser dispenser, MMOLineConfig config) {
        super(dispenser, config);

        config.validate("id");
        id = config.getInt("id", 1);
        silkTouch = config.getBoolean("silk-touch", true);
        playerPlaced = config.getBoolean("player-placed", false);
    }

    @Override
    public ExperienceSourceManager<MineMIBlockExperienceSource> newManager() {
        return new ExperienceSourceManager<MineMIBlockExperienceSource>() {

            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void a(BlockBreakEvent event) {
                if (event.getPlayer().getGameMode() != GameMode.SURVIVAL)
                    return;

                PlayerData data = PlayerData.get(event.getPlayer());
                Optional<CustomBlock> customBlock = MMOItems.plugin.getCustomBlocks().getFromBlock(event.getBlock().getBlockData());
                if (!customBlock.isPresent())
                    return;

                for (MineMIBlockExperienceSource source : getSources()) {
                    if (source.silkTouch && hasSilkTouch(event.getPlayer().getInventory().getItemInMainHand())
                            || (!source.playerPlaced) && event.getBlock().hasMetadata("player_placed"))
                        continue;

                    if (source.matches(data, customBlock.get().getId()))
                        source.giveExperience(data, 1, event.getBlock().getLocation());
                }
            }
        };
    }

    private boolean hasSilkTouch(ItemStack item) {
        return item != null && item.hasItemMeta() && item.getItemMeta().hasEnchant(Enchantment.SILK_TOUCH);
    }

    @Override
    public boolean matchesParameter(PlayerData player, Integer blockId) {
        return id == blockId;
    }
}
