package net.mmogroup.mmolib.listener.event;

import org.bukkit.Bukkit;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.AttackResult;
import net.mmogroup.mmolib.api.DamageType;
import net.mmogroup.mmolib.api.event.EntityKillEntityEvent;
import net.mmogroup.mmolib.api.event.PlayerAttackEvent;
import net.mmogroup.mmolib.api.player.MMOData;

public class PlayerAttackEventListener implements Listener {

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void a(EntityDamageByEntityEvent event) {
		if (!(event.getEntity() instanceof Damageable))
			return;

		/*
		 * check for event.
		 */
		AttackResult result = new AttackResult(event.getDamage());
		Entity damager = getDamager(result, event);

		/*
		 * check for last damage
		 */
		if (damager == null && (event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent))
			damager = getDamager(result, (EntityDamageByEntityEvent) event.getEntity().getLastDamageCause());

		/*
		 * if the damage source cannot be found, just return.
		 */
		if (damager == null)
			return;

		/*
		 * check damage systems from other MMOCore plugins + from MMOCore, and
		 * register an attack damage for easier plugin calculations
		 */
		if (damager instanceof Player && !damager.hasMetadata("NPC")) {
			AttackResult found = MMOLib.plugin.getDamage().findInfo(event.getEntity());
			if (found != null)
				result.getTypes().addAll(found.getTypes());

			/*
			 * if attack is not recognized, weapon by default.
			 */
			else
				result.getTypes().add(DamageType.WEAPON);

			PlayerAttackEvent attackEvent = new PlayerAttackEvent(MMOData.get((Player) damager), event, result);
			Bukkit.getPluginManager().callEvent(attackEvent);
			if (attackEvent.isCancelled())
				return;
			
			event.setDamage(result.getDamage());
		}

		/*
		 * checks for killing
		 */
		if (event.getFinalDamage() >= ((Damageable) event.getEntity()).getHealth())
			Bukkit.getPluginManager().callEvent(new EntityKillEntityEvent(damager, event.getEntity()));
	}

	private Entity getDamager(AttackResult result, EntityDamageByEntityEvent event) {

		/*
		 * check direct damager
		 */
		if (event.getDamager() instanceof LivingEntity)
			return event.getDamager();

		/*
		 * checks projectile and add damage type, which supports every vanilla
		 * projectile like snowballs, tridents and arrows
		 */
		if (event.getDamager() instanceof Projectile) {
			Projectile proj = (Projectile) event.getDamager();
			if (proj.getShooter() instanceof Entity) {
				result.getTypes().add(DamageType.PROJECTILE);
				return (Entity) proj.getShooter();
			}
		}

		return null;
	}
}
