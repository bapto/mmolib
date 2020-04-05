package net.mmogroup.mmolib.api.player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import net.mmogroup.mmolib.api.stat.StatMap;

public class MMOData {

	/*
	 * MMOCore and MMOItems data cached for easier access. only initialized on
	 * PlayerJoinEvent at priority NORMAL or higher
	 */
	private Object mmocore, mmoitems;

	/*
	 * stat data saved till next server startup
	 */
	private final StatMap stats = new StatMap(this);

	private final Map<MitigationType, Long> cooldowns = new HashMap<>();

	private static final Map<UUID, MMOData> data = new HashMap<>();

	public StatMap getStatMap() {
		return stats;
	}

	public net.Indyuce.mmocore.api.player.PlayerData getMMOCore() {
		return (net.Indyuce.mmocore.api.player.PlayerData) mmocore;
	}

	public net.Indyuce.mmoitems.api.player.PlayerData getMMOItems() {
		return (net.Indyuce.mmoitems.api.player.PlayerData) mmoitems;
	}

	public MMOData setMMOCore(Object mmocore) {
		this.mmocore = mmocore;
		return this;
	}

	public MMOData setMMOItems(Object mmoitems) {
		this.mmoitems = mmoitems;
		return this;
	}

	public void applyCooldown(MitigationType cd, double value) {
		cooldowns.put(cd, (long) (System.currentTimeMillis() + value * 1000));
	}

	public boolean isOnCooldown(MitigationType cd) {
		return cooldowns.containsKey(cd) && cooldowns.get(cd) > System.currentTimeMillis();
	}

	public Player getPlayer() {
		return mmocore != null ? ((net.Indyuce.mmocore.api.player.PlayerData) mmocore).getPlayer() : ((net.Indyuce.mmoitems.api.player.PlayerData) mmoitems).getPlayer();
	}

	public static void setup(OfflinePlayer player) {
		if (!data.containsKey(player.getUniqueId()))
			data.put(player.getUniqueId(), new MMOData());
	}

	public static MMOData get(OfflinePlayer player) {
		return data.get(player.getUniqueId());
	}
}
