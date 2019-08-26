package net.Indyuce.mmoitems.comp.flags;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;

public class ResidenceFlags implements FlagPlugin {
	public ResidenceFlags() {
		for (CustomFlag flag : CustomFlag.values())
			FlagPermissions.addFlag(flag.getPath());
	}

	@Override
	public boolean isPvpAllowed(Location loc) {
		ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(loc);
		return res == null || res.getPermissions().has(Flags.pvp, true);
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean isFlagAllowed(Player player, CustomFlag flag) {
		ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(player);
		return res == null || res.getPermissions().playerHas(player, flag.getPath(), true);
	}

	@Override
	public boolean isFlagAllowed(Location loc, CustomFlag flag) {
		ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(loc);
		return res == null || res.getPermissions().has(flag.getPath(), true, true);
	}
}