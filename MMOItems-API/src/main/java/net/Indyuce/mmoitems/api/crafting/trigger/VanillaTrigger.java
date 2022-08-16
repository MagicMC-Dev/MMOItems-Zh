package net.Indyuce.mmoitems.api.crafting.trigger;

import net.Indyuce.mmoitems.api.player.PlayerData;
import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.api.util.SmartGive;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class VanillaTrigger extends Trigger {
    private final Material material;
    private final int amount;

    public VanillaTrigger(MMOLineConfig config) {
        super("vanilla");

        config.validate("type");

        material = Material.valueOf(config.getString("type").toUpperCase().replace("-", "_"));
        amount = config.contains("amount") ? Math.max(1, config.getInt("amount")) : 1;
    }

    @Override
    public void whenCrafting(PlayerData data) {
    	if(!data.isOnline()) return;
        new SmartGive(data.getPlayer()).give(new ItemStack(material, amount));
    }
}
