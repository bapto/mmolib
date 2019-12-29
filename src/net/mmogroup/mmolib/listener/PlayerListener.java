package net.mmogroup.mmolib.listener;

import java.util.Random;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.DamageType;
import net.mmogroup.mmolib.api.event.PlayerAttackEvent;
import net.mmogroup.mmolib.api.player.MMOData;
import net.mmogroup.mmolib.api.stat.StatMap;

public class PlayerListener implements Listener {
	private static final Random random = new Random();

	private final double critCoefficient, spellCritCoefficient;

	public PlayerListener() {
		critCoefficient = MMOLib.plugin.getConfig().getDouble("crit-coefficient");
		spellCritCoefficient = MMOLib.plugin.getConfig().getDouble("spell-crit-coefficient");
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void a(PlayerJoinEvent event) {
		MMOData.setup(event.getPlayer());
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void b(PlayerAttackEvent event) {

		/*
		 * damage modifiers. they do not stack geometrically (not 1.1 x 1.2 ...
		 * but rather * (1 + ( 0.1 + 0.2 ... )). damage is multiplicated after
		 * adding up all the percent based damage stats
		 */
		double d = 0;
		StatMap stats = event.getData().getStatMap();
		for (DamageType type : event.getAttack().getTypes())
			d += stats.getStat(type.getStat()) / 100;

		if (MMOLib.plugin.getVersion().getWrapper().isUndead(event.getEntity()))
			d += stats.getStat("UNDEAD_DAMAGE") / 100;
		d += stats.getStat(event.getEntity() instanceof Player ? "PVP_DAMAGE" : "PVE_DAMAGE") / 100;
		event.getAttack().multiplyDamage(1 + d);

		/*
		 * weapon critical strikes
		 */
		if (event.getAttack().hasType(DamageType.WEAPON) && random.nextDouble() <= stats.getStat("CRITICAL_STRIKE_CHANCE") / 100) {
			event.getAttack().multiplyDamage(critCoefficient + stats.getStat("CRITICAL_STRIKE_POWER") / 100);
			event.getEntity().getWorld().playSound(event.getEntity().getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1, 1);
			event.getEntity().getWorld().spawnParticle(Particle.FIREWORKS_SPARK, event.getEntity().getLocation().add(0, 1, 0), 16, 0, 0, 0, .1);
		}

		/*
		 * skill critical strikes
		 */
		if (event.getAttack().hasType(DamageType.SKILL) && random.nextDouble() <= stats.getStat("SPELL_CRITICAL_STRIKE_CHANCE") / 100) {
			event.getAttack().multiplyDamage(spellCritCoefficient + stats.getStat("SPELL_CRITICAL_STRIKE_POWER") / 100);
			event.getEntity().getWorld().playSound(event.getEntity().getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1, 2);
			event.getEntity().getWorld().spawnParticle(Particle.TOTEM, event.getEntity().getLocation().add(0, 1, 0), 32, 0, 0, 0, .4);
		}
	}
}
