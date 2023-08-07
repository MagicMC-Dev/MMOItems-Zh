package net.Indyuce.mmoitems.comp.eco;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.crafting.ConditionalDisplay;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import io.lumine.mythic.lib.api.util.AltChar;

public class VaultSupport {
	private final Economy economy;
	private final Permission permissions;

	public VaultSupport() {
		RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().getServicesManager()
				.getRegistration(Economy.class);
		economy = economyProvider != null ? economyProvider.getProvider() : null;
		RegisteredServiceProvider<Permission> permissionProvider = Bukkit.getServer().getServicesManager()
				.getRegistration(Permission.class);
		permissions = permissionProvider != null ? permissionProvider.getProvider() : null;

		if(economy == null) {
			MMOItems.plugin.getLogger().log(Level.SEVERE, "Could not load Economy Support (Vault)");
		} else
			MMOItems.plugin.getCrafting().registerCondition("money", MoneyCondition::new,
					new ConditionalDisplay("&a" + AltChar.check + " Requires $#money#",
							"&c" + AltChar.cross + " Requires $#money#"));
		if(permissions == null) {
			MMOItems.plugin.getLogger().log(Level.SEVERE, "Could not load Permissions Support (Vault)");
		}

		if(economy != null || permissions != null)
			MMOItems.plugin.getLogger().log(Level.INFO, "Hooked onto Vault");
	}
	
	public Permission getPermissions() {
		return permissions;
	}
	
	public Economy getEconomy() {
		return economy;
	}
}
