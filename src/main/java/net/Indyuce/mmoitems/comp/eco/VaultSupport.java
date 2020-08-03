package net.Indyuce.mmoitems.comp.eco;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.crafting.ConditionalDisplay;
import net.milkbowl.vault.economy.Economy;
import net.mmogroup.mmolib.api.util.AltChar;

public class VaultSupport {
	private final Economy economy;

	public VaultSupport() {

		RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().getServicesManager()
				.getRegistration(Economy.class);
		economy = economyProvider != null ? economyProvider.getProvider() : null;

		if (economy == null) {
			MMOItems.plugin.getLogger().log(Level.SEVERE, "Could not load Vault");
			return;
		}

		MMOItems.plugin.getCrafting().registerCondition("money", config -> new MoneyCondition(economy, config),
				new ConditionalDisplay("&a" + AltChar.check + " Requires $#money#",
						"&c" + AltChar.cross + " Requires $#money#"));
	}
}
