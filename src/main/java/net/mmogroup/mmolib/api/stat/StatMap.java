package net.mmogroup.mmolib.api.stat;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.bukkit.attribute.Attribute;

import net.mmogroup.mmolib.api.player.MMOData;
import net.mmogroup.mmolib.api.stat.handler.AttributeStatHandler;
import net.mmogroup.mmolib.api.stat.handler.MovementSpeedStatHandler;
import net.mmogroup.mmolib.api.stat.instance.SimpleStatInstance;

public class StatMap {

	/*
	 * instance which saves player data from both MMOCore and MMOItems to let
	 * other plugins retrieve data
	 */
	private final MMOData player;
	
	private final Map<String, StatInstance> stats = new HashMap<>();

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

	/*
	 * all updates which should be ran whenever a stat is updated so that
	 * MMOCore and MMOItems know the stat was updated.
	 */
	private static final Map<String, Consumer<MMOData>> updates = new HashMap<>();

	static {
		updates.put(SharedStat.ARMOR, new AttributeStatHandler(Attribute.GENERIC_ARMOR, SharedStat.ARMOR));
		updates.put(SharedStat.ARMOR_TOUGHNESS, new AttributeStatHandler(Attribute.GENERIC_ARMOR_TOUGHNESS, SharedStat.ARMOR));

		updates.put(SharedStat.ATTACK_DAMAGE, new AttributeStatHandler(Attribute.GENERIC_ATTACK_DAMAGE, SharedStat.ATTACK_DAMAGE));
		updates.put(SharedStat.ATTACK_SPEED, new AttributeStatHandler(Attribute.GENERIC_ATTACK_SPEED, SharedStat.ATTACK_SPEED));
		updates.put(SharedStat.KNOCKBACK_RESISTANCE, new AttributeStatHandler(Attribute.GENERIC_KNOCKBACK_RESISTANCE, SharedStat.KNOCKBACK_RESISTANCE));
		updates.put(SharedStat.MAX_HEALTH, new AttributeStatHandler(Attribute.GENERIC_MAX_HEALTH, SharedStat.MAX_HEALTH));

		Consumer<MMOData> moveSpeed = new MovementSpeedStatHandler();
		updates.put(SharedStat.MOVEMENT_SPEED, moveSpeed);
		updates.put(SharedStat.SPEED_MALUS_REDUCTION, moveSpeed);
	}

	/*
	 * used to generate different stat instances based on the plugins installed
	 * on the server. stat instances are used to calculate the stat base value,
	 * which must be calculated based on the player class if MMOCore is
	 * installed, or straight up 0 if not
	 */
	private static BiFunction<StatMap, String, StatInstance> instanceGenerator = (map, stat) -> new SimpleStatInstance(map, stat);

	public static void setInstanceGenerator(BiFunction<StatMap, String, StatInstance> instanceGenerator) {
		StatMap.instanceGenerator = instanceGenerator;
	}
}
