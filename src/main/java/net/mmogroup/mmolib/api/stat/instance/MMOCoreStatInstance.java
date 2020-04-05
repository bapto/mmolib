package net.mmogroup.mmolib.api.stat.instance;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.stats.StatType;
import net.mmogroup.mmolib.api.stat.StatInstance;
import net.mmogroup.mmolib.api.stat.StatMap;

public class MMOCoreStatInstance extends StatInstance {
	private final StatType stat;

	public MMOCoreStatInstance(StatMap map, StatType stat) {
		super(map, stat.name());

		this.stat = stat;
	}

	@Override
	public double getBase() {
		PlayerData data = getMap().getPlayerData().getMMOCore();
		return data.getProfess().calculateStat(stat, stat.hasProfession() ? data.getCollectionSkills().getLevel(stat.getProfession()) : data.getLevel());
	}
}
