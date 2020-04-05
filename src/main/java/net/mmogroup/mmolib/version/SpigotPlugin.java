package net.mmogroup.mmolib.version;

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
	private final JavaPlugin plugin;
	private final int id;

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

			if (!isOutdated(version, plugin.getDescription().getVersion()))
				return;

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

	private boolean isOutdated(String v1, String v2) {

		// easy check first
		if (v1.equals(v2))
			return false;

		String[] netVersion = v1.replaceAll("[^0-9.]", "").split("\\.");
		String[] localVersion = v2.replaceAll("[^0-9.]", "").split("\\.");

		/*
		 * no need to try and catch parsing exceptions because of the previous
		 * regex filter
		 */
		for (int i = 0; i < Math.max(netVersion.length, localVersion.length); i++)
			if ((i >= netVersion.length ? 0 : Integer.parseInt(netVersion[i])) > (i >= localVersion.length ? 0 : Integer.parseInt(localVersion[i])))
				return true;

		return false;
	}

	private List<String> getOutOfDateMessage() {
		return Arrays.asList("&8--------------------------------------------", "&a" + plugin.getName() + " " + version + " is available!", "&a" + getResourceUrl(), "&7&oYou can disable this notification in the config file.", "&8--------------------------------------------");
	}

	private String getResourceUrl() {
		return "https://www.spigotmc.org/resources/" + id + "/";
	}
}
