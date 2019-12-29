package net.mmogroup.mmolib.api.stat.instance;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.stats.StatType;
import net.mmogroup.mmolib.api.stat.StatInstance;
import net.mmogroup.mmolib.api.stat.StatMap;

public class MMOCoreStatInstance extends StatInstance {
	private StatType stat;

	public MMOCoreStatInstance(StatMap map, String stat) {
		super(map, stat);

		try {
			this.stat = StatType.valueOf(stat);
		} catch (IllegalArgumentException notMMOCoreStat) {
		}
	}

	@Override
	public double getBase() {

		/*
		 * if it is not a MMOCore stat just return 0 as base
		 */
		if (stat == null)
			return 0;

		PlayerData data = getMap().getPlayerData().getMMOCore();
		return data.getProfess().calculateStat(stat, stat.hasProfession() ? data.getCollectionSkills().getLevel(stat.getProfession()) : data.getLevel());
	}
}
