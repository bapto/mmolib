package net.mmogroup.mmolib.api.stat;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.mmogroup.mmolib.api.stat.modifier.StatModifier;
import net.mmogroup.mmolib.api.stat.modifier.TemporaryStatModifier;

public abstract class StatInstance {
	private final StatMap map;
	private final String stat;
	private final Map<String, StatModifier> modifiers = new HashMap<>();

	public StatInstance(StatMap map, String stat) {
		this.map = map;
		this.stat = stat;
	}

	public StatMap getMap() {
		return map;
	}

	public String getStat() {
		return stat;
	}

	public abstract double getBase();

	public double getTotal() {
		return getTotal(getBase());
	}

	/*
	 * 1) two types of attributes: flat attributes which add X to the value, and
	 * relative attributes which add X% and which must be applied afterwards 2)
	 * the 'd' parameter lets you choose if the relative attributes also apply
	 * on the base stat, or if they only apply on the extra stat value
	 */
	public double getTotal(double d) {

		for (StatModifier attr : modifiers.values())
			if (attr.isRelative())
				d = attr.apply(d);

		for (StatModifier attr : modifiers.values())
			if (!attr.isRelative())
				d = attr.apply(d);

		return d;
	}

	public StatModifier getAttribute(String key) {
		return modifiers.get(key);
	}

	public void addModifier(String key, double value) {
		addModifier(key, new StatModifier(value));
	}

	public void applyTemporaryModifier(String key, StatModifier modifier, long duration) {
		addModifier(key, new TemporaryStatModifier(modifier.getValue(), duration, modifier.isRelative(), key, this));
	}

	public void addModifier(String key, StatModifier modifier) {
		modifiers.put(key, modifier);

		map.update(stat);
	}

	public Set<String> getKeys() {
		return modifiers.keySet();
	}

	public boolean contains(String key) {
		return modifiers.containsKey(key);
	}

	public void remove(String key) {

		/*
		 * closing stat is really important with temporary stats because
		 * otherwise the runnable will try to remove the key from the map even
		 * though the attribute was cancelled before hand
		 */
		if (modifiers.containsKey(key)) {
			modifiers.get(key).close();
			modifiers.remove(key);
		}

		map.update(stat);
	}
}
