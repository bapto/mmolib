package net.mmogroup.mmolib.api.stat.instance;

import java.util.HashMap;
import java.util.Map;

import net.mmogroup.mmolib.api.stat.StatInstance;
import net.mmogroup.mmolib.api.stat.StatMap;

public class SimpleStatInstance extends StatInstance {

	/*
	 * (bug fix) also has to take into account ATTRIBUTE based stats which
	 * default values must NOT be 0. MMOCoreStatInstances do not have to worry
	 * about these because base values can be configured in the MMOCOre configs.
	 */
	private static final Map<String, Double> baseValues = new HashMap<>();

	static {
		baseValues.put("ATTACK_DAMAGE", 1d);
		baseValues.put("ATTACK_SPEED", 4d);
		baseValues.put("MAX_HEALTH", 20d);
		baseValues.put("MOVEMENT_SPEED", .2d);
	}

	public SimpleStatInstance(StatMap map, String stat) {
		super(map, stat);
	}

	@Override
	public double getBase() {
		return baseValues.containsKey(getStat()) ? baseValues.get(getStat()) : 0;
	}
}
