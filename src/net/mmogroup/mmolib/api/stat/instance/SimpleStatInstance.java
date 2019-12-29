package net.mmogroup.mmolib.api.stat.instance;

import net.mmogroup.mmolib.api.stat.StatInstance;
import net.mmogroup.mmolib.api.stat.StatMap;

public class SimpleStatInstance extends StatInstance {
	public SimpleStatInstance(StatMap map, String stat) {
		super(map, stat);
	}

	@Override
	public double getBase() {
		return 0;
	}
}
