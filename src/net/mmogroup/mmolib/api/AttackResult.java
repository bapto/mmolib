package net.mmogroup.mmolib.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import net.mmogroup.mmolib.MMOLib;

public class AttackResult {
	private double damage;
	private boolean successful;

	private final Set<DamageType> damageTypes;

	public AttackResult(boolean successful, DamageType... types) {
		this(successful, 0, types);
	}

	public AttackResult(double damage, DamageType... types) {
		this(true, damage, types);
	}

	public AttackResult(boolean successful, double damage, DamageType... types) {
		this(successful, damage, new HashSet<>(Arrays.asList(types)));
	}

	public AttackResult(boolean successful, double damage, Set<DamageType> types) {
		this.successful = successful;
		this.damage = damage;
		this.damageTypes = types;
	}

	public AttackResult(AttackResult result) {
		damage = result.damage;
		successful = result.successful;
		damageTypes = new HashSet<>(result.damageTypes);
	}

	public Set<DamageType> getTypes() {
		return damageTypes;
	}

	public boolean hasType(DamageType type) {
		return damageTypes.contains(type);
	}

	public boolean isSuccessful() {
		return successful;
	}

	public double getDamage() {
		return damage;
	}

	public AttackResult addDamage(double value) {
		damage += value;
		return this;
	}

	public AttackResult multiplyDamage(double coef) {
		damage *= coef;
		return this;
	}

	public AttackResult setSuccessful(boolean successful) {
		this.successful = successful;
		return this;
	}

	public AttackResult clone() {
		return new AttackResult(this);
	}

	public void damage(Player player, LivingEntity target) {
		MMOLib.plugin.getDamage().damage(player, target, this);
	}
}
