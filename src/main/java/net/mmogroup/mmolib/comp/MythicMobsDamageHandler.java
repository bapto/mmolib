package net.mmogroup.mmolib.comp;

import org.bukkit.entity.Entity;

import net.mmogroup.mmolib.api.AttackResult;
import net.mmogroup.mmolib.api.DamageHandler;
import net.mmogroup.mmolib.api.DamageType;

public class MythicMobsDamageHandler implements DamageHandler {

	@Override
	public AttackResult getDamage(Entity entity) {
		return new AttackResult(0, DamageType.MAGIC);
	}

	@Override
	public boolean hasDamage(Entity entity) {
		return entity.hasMetadata("skill-damage");
	}
}
