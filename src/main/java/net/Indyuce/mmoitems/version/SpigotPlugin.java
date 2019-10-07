package net.Indyuce.mmoitems.version;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import javax.net.ssl.HttpsURLConnection;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class SpigotPlugin {
	private JavaPlugin plugin;
	private int id;
	private String version;

	public SpigotPlugin(int id, JavaPlugin plugin) {
		this.plugin = plugin;
		this.id = id;
	}

	/*
	 * the request is executed asynchronously as not to block the main thread.
	 */
	public void checkForUpdate() {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			try {
				HttpsURLConnection connection = (HttpsURLConnection) new URL("https://api.spigotmc.org/legacy/update.php?resource=" + id).openConnection();
				connection.setRequestMethod("GET");
				version = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();
			} catch (IOException e) {
				plugin.getLogger().log(Level.INFO, "Couldn't check the latest plugin version :/");
				return;
			}

			if (!isOldVersion(version, plugin.getDescription().getVersion())) return;
			
			plugin.getLogger().log(Level.INFO, "A new build is available: " + version + " (you are running " + plugin.getDescription().getVersion() + ")");
			plugin.getLogger().log(Level.INFO, "Download it here: " + getResourceUrl());

			/*
			 * registers the event to notify op players when they join only if
			 * the corresponding option is enabled
			 */
			if (plugin.getConfig().getBoolean("update-notify"))
				Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().registerEvents(new Listener() {
					@EventHandler(priority = EventPriority.MONITOR)
					public void onPlayerJoin(PlayerJoinEvent event) {
						Player player = event.getPlayer();
						if (player.hasPermission(plugin.getName().toLowerCase() + ".update-notify"))
							getOutOfDateMessage().forEach(msg -> player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg)));
					}
				}, plugin));
		});
	}

	private boolean isOldVersion(String v1, String v2) {
		if(v1.equals(v2)) return false;
		String[] netVersion = v1.replaceAll("[^0-9.]", "").split("\\.");
		String[] localVersion = v2.replaceAll("[^0-9.]", "").split("\\.");
		
		int netVersionFirst = parse(netVersion[0]);
		int localVersionFirst = parse(localVersion[0]);

		if(netVersionFirst < localVersionFirst) return false;
		if(netVersionFirst > localVersionFirst) return true;
		
		int netVersionMiddle = parse(netVersion[1]);
		int localVersionMiddle = parse(localVersion[1]);

		if(netVersionMiddle < localVersionMiddle) return false;
		if(netVersionMiddle > localVersionMiddle) return true;

		int netVersionLast = netVersion.length > 1 ? parse(netVersion[2]) : 0;
		int localVersionLast = localVersion.length > 1 ? parse(localVersion[2]) : 0;

		if(netVersionLast < localVersionLast) return false;
		return true;
	}
	
	private int parse(String s) {
		try {
			return Integer.parseInt(s);
		}
		catch(NumberFormatException e) {
			plugin.getLogger().warning("Something went wrong checking the version numbers!");
			return 0;
		}
	}

	private List<String> getOutOfDateMessage() {
		return Arrays.asList("&8--------------------------------------------", "&a" + plugin.getName() + " " + version + " is available!", "&a" + getResourceUrl(), "&7&oYou can disable this notification in the config file.", "&8--------------------------------------------");
	}
	public String getResourceUrl() {
		return "https://www.spigotmc.org/resources/" + id + "/";
	}
}
