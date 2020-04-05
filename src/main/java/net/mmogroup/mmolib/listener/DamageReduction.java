package net.mmogroup.mmolib.listener;

import java.util.function.BiFunction;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.AttackResult;
import net.mmogroup.mmolib.api.DamageType;
import net.mmogroup.mmolib.api.player.MMOData;
import net.mmogroup.mmolib.api.stat.StatMap;

public class DamageReduction implements Listener {

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void a(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player) || event.getEntity().hasMetadata("NPC"))
			return;

		MMOData data = MMOData.get((OfflinePlayer) event.getEntity());
		DamageReductionCalculator calculator = new DamageReductionCalculator(data);
		AttackResult res = MMOLib.plugin.getDamage().findInfo(event.getEntity());

		for (DamageReductionType type : DamageReductionType.values())
			if (type.isApplicable(res, event))
				calculator.applyReduction(type);

		event.setDamage(event.getDamage() * calculator.getCoefficient());
	}

	/*
	 * util class to help and easily manage damage reduction formulas. currently
	 * %'s do not add up which means 30% + 30% is not 60% but 51% to prevent OP
	 * damage reduction stats.
	 */
	public class DamageReductionCalculator {
		private final StatMap stats;

		private double c = 1;

		public DamageReductionCalculator(MMOData data) {
			this.stats = data.getStatMap();
		}

		public void applyReduction(DamageReductionType type) {
			c *= 1 - Math.min(1, Math.max(0, stats.getStat(type.getStat()) / 100));
		}

		double getCoefficient() {
			return c;
		}
	}

	/*
	 * all different types of damage reduction are listed here
	 */
	public enum DamageReductionType {

		/*
		 * damage reduction, always applies
		 */
		ENVIRONMENTAL("DAMAGE_REDUCTION", (result, event) -> true),

		/*
		 * fight based damage reduction types
		 */
		PVP((result, event) -> event instanceof EntityDamageByEntityEvent && getDamager((EntityDamageByEntityEvent) event) instanceof Player),
		PVE((result, event) -> event instanceof EntityDamageByEntityEvent && !(getDamager((EntityDamageByEntityEvent) event) instanceof Player)),

		/*
		 * simple damage reduction types
		 */
		FIRE((result, event) -> event.getCause() == DamageCause.FIRE || event.getCause() == DamageCause.FIRE_TICK),
		FALL((result, event) -> event.getCause() == DamageCause.FALL),

		/*
		 * damage type based damage reduction types
		 */
		MAGIC((result, event) -> event.getCause() == DamageCause.MAGIC || (result != null && result.hasType(DamageType.MAGIC))),
		PHYSICAL((result, event) -> event instanceof EntityDamageByEntityEvent || (result != null && result.hasType(DamageType.PHYSICAL))),
		WEAPON((result, event) -> result != null && result.hasType(DamageType.WEAPON)),
		SKILL((result, event) -> result != null && result.hasType(DamageType.SKILL)),
		PROJECTILE((result, event) -> (event instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) event).getDamager() instanceof Projectile) || (result != null && result.hasType(DamageType.PROJECTILE)));

		private final String stat;
		private final BiFunction<AttackResult, EntityDamageEvent, Boolean> apply;

		private DamageReductionType(String stat, BiFunction<AttackResult, EntityDamageEvent, Boolean> apply) {
			this.stat = stat;
			this.apply = apply;
		}

		private DamageReductionType(BiFunction<AttackResult, EntityDamageEvent, Boolean> apply) {
			this.stat = name() + "_DAMAGE_REDUCTION";
			this.apply = apply;
		}

		public boolean isApplicable(AttackResult result, EntityDamageEvent event) {
			return apply.apply(result, event);
		}

		public String getStat() {
			return stat;
		}
	}

	private static LivingEntity getDamager(EntityDamageByEntityEvent event) {

		/*
		 * check direct damager
		 */
		if (event.getDamager() instanceof LivingEntity)
			return (LivingEntity) event.getDamager();

		/*
		 * checks projectile and add damage type, which supports every vanilla
		 * projectile like snowballs, tridents and arrows
		 */
		if (event.getDamager() instanceof Projectile) {
			Projectile proj = (Projectile) event.getDamager();
			if (proj.getShooter() instanceof LivingEntity)
				return (LivingEntity) proj.getShooter();
		}

		/*
		 * check for last damage
		 */
		// if (event.getEntity().getLastDamageCause() instanceof
		// EntityDamageByEntityEvent && checkLastDamageCause)
		// return getDamager(result, (EntityDamageByEntityEvent)
		// event.getEntity().getLastDamageCause(), false);

		return null;
	}
}
