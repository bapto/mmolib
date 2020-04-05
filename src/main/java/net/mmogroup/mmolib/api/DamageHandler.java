package net.mmogroup.mmolib.api;

import org.bukkit.entity.Entity;

public interface DamageHandler {
	public AttackResult getDamage(Entity entity);

	public boolean hasDamage(Entity entity);
}
