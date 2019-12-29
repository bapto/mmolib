package net.mmogroup.mmolib.api.stat;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import net.mmogroup.mmolib.api.player.MMOData;
import net.mmogroup.mmolib.api.stat.instance.SimpleStatInstance;

public class StatMap {

	/*
	 * instance which saves player data from both MMOCore and MMOItems to let
	 * other plugins retrieve data
	 */
	private final MMOData player;

	private final Map<String, StatInstance> stats = new HashMap<>();

	/*
	 * all updates which should be ran whenever a stat is updated so that
	 * MMOCore and MMOItems know the stat was updated.
	 */
	private static final Map<String, Consumer<MMOData>> updates = new HashMap<>();
	private static BiFunction<StatMap, String, StatInstance> instanceGenerator = (map, stat) -> new SimpleStatInstance(map, stat);

	public StatMap(MMOData player) {
		this.player = player;
	}

	public MMOData getPlayerData() {
		return player;
	}
	
	public double getStat(String id) {
		return getInstance(id).getTotal();
	}

	public StatInstance getInstance(String id) {
		if (stats.containsKey(id))
			return stats.get(id);

		StatInstance ins = instanceGenerator.apply(this, id);
		stats.put(id, ins);
		return ins;
	}

	public Collection<StatInstance> getInstances() {
		return stats.values();
	}

	public void updateAll() {
		updates.values().forEach(consumer -> consumer.accept(player));
	}

	public void update(String stat) {
		if (updates.containsKey(stat))
			updates.get(stat).accept(player);
	}

	public static void registerUpdate(String stat, Consumer<MMOData> action) {
		if (!updates.containsKey(stat))
			updates.put(stat, action);
	}

	public static void setInstanceGenerator(BiFunction<StatMap, String, StatInstance> instanceGenerator) {
		StatMap.instanceGenerator = instanceGenerator;
	}
}
