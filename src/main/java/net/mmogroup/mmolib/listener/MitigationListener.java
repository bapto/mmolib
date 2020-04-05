package net.mmogroup.mmolib.listener;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.event.mitigation.PlayerBlockEvent;
import net.mmogroup.mmolib.api.event.mitigation.PlayerDodgeEvent;
import net.mmogroup.mmolib.api.event.mitigation.PlayerParryEvent;
import net.mmogroup.mmolib.api.player.MMOData;
import net.mmogroup.mmolib.api.player.MitigationType;
import net.mmogroup.mmolib.api.stat.StatMap;
import net.mmogroup.mmolib.version.VersionSound;

public class MitigationListener implements Listener {
	private static final Random random = new Random();
	private static final List<DamageCause> mitigationCauses = Arrays.asList(DamageCause.PROJECTILE, DamageCause.ENTITY_ATTACK, DamageCause.ENTITY_EXPLOSION, DamageCause.ENTITY_SWEEP_ATTACK);
	private static final DecimalFormat digit = new DecimalFormat("0.#");

	private final boolean dodgeKnockbackEnabled, parryKnockbackEnabled, actionBarMessage;
	private final double dodgeKnockbackForce, maxDodgeRating, maxParryRating, maxBlockRating, maxBlockPower, defaultBlockPower, parryKnockbackForce;
	private final double parryDefaultCooldown, blockDefaultCooldown, dodgeDefaultCooldown, parryMinCooldown, blockMinCooldown, dodgeMinCooldown;
	private final String parryMessage, blockMessage, dodgeMessage;

	public MitigationListener() {
		dodgeKnockbackEnabled = MMOLib.plugin.getConfig().getBoolean("mitigation.dodge.knockback.enabled");
		dodgeKnockbackForce = MMOLib.plugin.getConfig().getDouble("mitigation.dodge.knockback.force");

		maxDodgeRating = MMOLib.plugin.getConfig().getDouble("mitigation.dodge.rating-max");
		maxParryRating = MMOLib.plugin.getConfig().getDouble("mitigation.parry.rating-max");
		maxBlockRating = MMOLib.plugin.getConfig().getDouble("mitigation.block.rating-max");
		maxBlockPower = MMOLib.plugin.getConfig().getDouble("mitigation.block.power.max");
		defaultBlockPower = MMOLib.plugin.getConfig().getDouble("mitigation.block.power.default");

		parryDefaultCooldown = MMOLib.plugin.getConfig().getDouble("mitigation.parry.cooldown.default");
		blockDefaultCooldown = MMOLib.plugin.getConfig().getDouble("mitigation.block.cooldown.default");
		dodgeDefaultCooldown = MMOLib.plugin.getConfig().getDouble("mitigation.dodge.cooldown.default");
		parryMinCooldown = MMOLib.plugin.getConfig().getDouble("mitigation.parry.cooldown.min");
		blockMinCooldown = MMOLib.plugin.getConfig().getDouble("mitigation.block.cooldown.min");
		dodgeMinCooldown = MMOLib.plugin.getConfig().getDouble("mitigation.dodge.cooldown.min");

		parryKnockbackEnabled = MMOLib.plugin.getConfig().getBoolean("mitigation.parry.knockback.enabled");
		parryKnockbackForce = MMOLib.plugin.getConfig().getDouble("mitigation.parry.knockback.force");

		parryMessage = MMOLib.plugin.getConfig().getString("mitigation.message.parry");
		dodgeMessage = MMOLib.plugin.getConfig().getString("mitigation.message.dodge");
		blockMessage = MMOLib.plugin.getConfig().getString("mitigation.message.block");
		actionBarMessage = MMOLib.plugin.getConfig().getBoolean("mitigation.message.action-bar");
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void a(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player) || !mitigationCauses.contains(event.getCause()) || event.getEntity().hasMetadata("NPC"))
			return;

		Player player = (Player) event.getEntity();
		MMOData playerData = MMOData.get(player);
		StatMap stats = playerData.getStatMap();

		/*
		 * dodging
		 */
		double dodgeRating = Math.min(stats.getStat("DODGE_RATING"), maxDodgeRating) / 100;
		if (random.nextDouble() < dodgeRating && !playerData.isOnCooldown(MitigationType.DODGE)) {

			PlayerDodgeEvent mitigationEvent = new PlayerDodgeEvent(playerData, event);
			Bukkit.getPluginManager().callEvent(mitigationEvent);
			if (mitigationEvent.isCancelled())
				return;

			sendMessage(player, dodgeMessage, "damage", digit.format(event.getFinalDamage()));
			playerData.applyCooldown(MitigationType.DODGE, calculateCooldown(dodgeDefaultCooldown, stats.getStat("DODGE_COOLDOWN_REDUCTION") / 100, dodgeMinCooldown));
			event.setCancelled(true);
			player.getWorld().playSound(player.getLocation(), VersionSound.ENTITY_ENDER_DRAGON_FLAP.toSound(), 2, 1);
			player.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, player.getLocation(), 16, 0, 0, 0, .06);
			if (dodgeKnockbackEnabled)
				player.setVelocity(getVector(player, event).multiply(.85 * dodgeKnockbackForce).setY(.3));
			return;
		}

		/*
		 * parrying
		 */
		double parryRating = Math.min(stats.getStat("PARRY_RATING"), maxParryRating) / 100;
		if (random.nextDouble() < parryRating && !playerData.isOnCooldown(MitigationType.PARRY)) {

			PlayerParryEvent mitigationEvent = new PlayerParryEvent(playerData, event);
			Bukkit.getPluginManager().callEvent(mitigationEvent);
			if (mitigationEvent.isCancelled())
				return;

			playerData.applyCooldown(MitigationType.PARRY, calculateCooldown(parryDefaultCooldown, stats.getStat("PARRY_COOLDOWN_REDUCTION") / 100, parryMinCooldown));
			event.setCancelled(true);
			sendMessage(player, parryMessage, "damage", digit.format(event.getFinalDamage()));
			player.getWorld().playSound(player.getLocation(), VersionSound.ENTITY_ENDER_DRAGON_FLAP.toSound(), 2, 1);
			player.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, player.getLocation(), 16, 0, 0, 0, .06);
			if (parryKnockbackEnabled && event instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) event).getDamager() instanceof LivingEntity) {
				LivingEntity attacker = (LivingEntity) ((EntityDamageByEntityEvent) event).getDamager();
				attacker.setVelocity(normalize(attacker.getLocation().toVector().subtract(player.getLocation().toVector())).setY(.35).multiply(parryKnockbackForce));
			}
			return;
		}

		/*
		 * blocking
		 */
		double blockRating = Math.min(stats.getStat("BLOCK_RATING"), maxBlockRating) / 100;
		if (random.nextDouble() < blockRating && !playerData.isOnCooldown(MitigationType.BLOCK)) {

			double blockPower = Math.min(defaultBlockPower + stats.getStat("BLOCK_POWER"), maxBlockPower) / 100;
			PlayerBlockEvent mitigationEvent = new PlayerBlockEvent(playerData, event, blockPower);
			Bukkit.getPluginManager().callEvent(mitigationEvent);
			if (mitigationEvent.isCancelled())
				return;

			playerData.applyCooldown(MitigationType.BLOCK, calculateCooldown(blockDefaultCooldown, stats.getStat("BLOCK_COOLDOWN_REDUCTION") / 100, blockMinCooldown));
			sendMessage(player, blockMessage, "damage", digit.format(mitigationEvent.getDamageBlocked()), "power", digit.format(mitigationEvent.getPower() * 100.));
			event.setDamage(event.getDamage() * (1 - mitigationEvent.getPower()));
			player.getWorld().playSound(player.getLocation(), VersionSound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR.toSound(), 2, 1);

			double yaw = getYaw(player, getVector(player, event)) - 90;
			for (double j = yaw - 90; j < yaw + 90; j += 5)
				for (double y = 0; y < 2; y += .1)
					MMOLib.plugin.getVersion().getWrapper().spawnParticle(Particle.REDSTONE, player.getLocation().clone().add(Math.cos(Math.toRadians(j)) * .7, y, Math.sin(Math.toRadians(j)) * .7), Color.GRAY);
		}
	}

	private void sendMessage(Player player, String format, Object... placeholders) {
		if (format == null || format.isEmpty())
			return;

		format = ChatColor.translateAlternateColorCodes('&', format);
		for (int i = 0; i < placeholders.length; i += 2)
			format = format.replace("#" + placeholders[i].toString() + "#", placeholders[i + 1].toString());

		if (actionBarMessage)
			MMOLib.plugin.getNMS().sendActionBar(player, format);
		else
			player.sendMessage(format);
	}

	private double calculateCooldown(double cooldown, double reduction, double min) {
		return Math.max(min, cooldown * (1 - reduction));
	}

	private Vector getVector(Player player, EntityDamageEvent event) {
		return event instanceof EntityDamageByEntityEvent ? normalize(player.getLocation().subtract(((EntityDamageByEntityEvent) event).getDamager().getLocation()).toVector()) : player.getEyeLocation().getDirection();
	}

	private Vector normalize(Vector vec) {
		return vec.lengthSquared() == 0 ? vec : vec.normalize();
	}

	private double getYaw(Entity player, Vector vec) {
		return new Location(player.getWorld(), vec.getX(), vec.getY(), vec.getZ()).setDirection(vec).getYaw();
	}
}
