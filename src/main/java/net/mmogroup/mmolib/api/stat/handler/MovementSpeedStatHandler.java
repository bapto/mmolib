package net.mmogroup.mmolib.api.stat.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.player.MMOData;
import net.mmogroup.mmolib.api.stat.SharedStat;

/*
 * both used for the 'movement speed' and for the 'speed malus reduction'
 * stats because the movement speed must be refreshed every time one of
 * these stats are changed.
 */
public class MovementSpeedStatHandler implements Consumer<MMOData> {
	private static final double speedMalus = MMOLib.plugin.getConfig().getDouble("heavy-armors.speed-malus") / 100;
	private static final List<String> heavyArmors = new ArrayList<>();

	static {
		if (MMOLib.plugin.getConfig().getStringList("heavy-armors.list") == null)
			MMOLib.plugin.getLogger().log(Level.INFO, "Could not load heavy armors list.");
		else
			MMOLib.plugin.getConfig().getStringList("heavy-armors.list").forEach(str -> {
				String format = str.toUpperCase().replace("-", "_").replace(" ", "_");
				try {
					Material.valueOf(format);
					heavyArmors.add(format);
				} catch (IllegalArgumentException exception) {
					MMOLib.plugin.getLogger().log(Level.INFO, "Could not read material from '" + format + "'");
				}
			});
	}

	@Override
	public void accept(MMOData data) {
		double speedMalus = MovementSpeedStatHandler.speedMalus * (1 - Math.max(0, Math.min(1, data.getStatMap().getStat(SharedStat.SPEED_MALUS_REDUCTION) / 100)));
		double movementSpeed = data.getStatMap().getStat(SharedStat.MOVEMENT_SPEED);

		for (ItemStack item : data.getPlayer().getEquipment().getArmorContents())
			if (item != null && heavyArmors.contains(item.getType().name()))
				movementSpeed *= 1 - speedMalus;
		data.getPlayer().setWalkSpeed((float) Math.min(1, movementSpeed));
	}
}