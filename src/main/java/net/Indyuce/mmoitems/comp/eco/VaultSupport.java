package net.Indyuce.mmoitems.comp.eco;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.crafting.ConditionalDisplay;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import net.mmogroup.mmolib.api.util.AltChar;

public class VaultSupport {
	private final Economy economy;
	private final Permission permissions;
	private final boolean enabled;

	public VaultSupport() {
		RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().getServicesManager()
				.getRegistration(Economy.class);
		economy = economyProvider != null ? economyProvider.getProvider() : null;
		RegisteredServiceProvider<Permission> permissionProvider = Bukkit.getServer().getServicesManager()
				.getRegistration(Permission.class);
		permissions = permissionProvider != null ? permissionProvider.getProvider() : null;
		
		enabled = load();
		if(!enabled) {
			MMOItems.plugin.getLogger().log(Level.SEVERE, "Could not load Vault");
			return;
		}

		MMOItems.plugin.getLogger().log(Level.INFO, "Hooked onto Vault");
		MMOItems.plugin.getCrafting().registerCondition("money", config -> new MoneyCondition(config),
				new ConditionalDisplay("&a" + AltChar.check + " Requires $#money#",
						"&c" + AltChar.cross + " Requires $#money#"));
	}
	
	private boolean load() {
		if (economy == null)
			return false;
		if (permissions == null)
			return false;
		
		return true;
	}
	
	public Permission getPermissions() {
		return permissions;
	}
	
	public Economy getEconomy() {
		return economy;
	}
	
	public boolean enabled() {
		return enabled;
	}
}
