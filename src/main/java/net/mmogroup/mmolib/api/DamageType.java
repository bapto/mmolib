package net.mmogroup.mmolib.api;

public enum DamageType {

	/*
	 * skills or abilities dealing magic damage
	 */
	MAGIC,

	/*
	 * skills or abilities dealing physical damage
	 */
	PHYSICAL,

	/*
	 * weapons dealing damage
	 */
	WEAPON,

	/*
	 * skill damage
	 */
	SKILL,

	/*
	 * projectile based weapons or skills
	 */
	PROJECTILE;

	public String getPath() {
		return name().toLowerCase();
	}

	public String getStat() {
		return name() + "_DAMAGE";
	}
}
