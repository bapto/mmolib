package net.mmogroup.mmolib.api.stat.modifier;

import org.bukkit.scheduler.BukkitRunnable;

import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.stat.StatInstance;


public class TemporaryStatModifier extends StatModifier {
	private final BukkitRunnable runnable;
	
	public TemporaryStatModifier(double d, long duration, boolean relative, String key, StatInstance ins) {
		super(d, relative);

		(runnable = new BukkitRunnable() {
			public void run() {
				ins.remove(key);
			}
		}).runTaskLater(MMOLib.plugin, duration);
	}

	public void close() {
		if (!runnable.isCancelled())
			runnable.cancel();
	}
}
