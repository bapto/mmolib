package net.mmogroup.mmolib.version.wrapper;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.CropState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Crops;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import net.minecraft.server.v1_12_R1.AxisAlignedBB;
import net.minecraft.server.v1_12_R1.MovingObjectPosition;
import net.minecraft.server.v1_12_R1.Vec3D;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.MMORayTraceResult;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.item.NBTItem;

@SuppressWarnings("deprecation")
public class VersionWrapper_Legacy implements VersionWrapper {

	@Override
	public void spawnParticle(Particle particle, Location loc, int amount, double x, double y, double z, double speed, float size, Color color) {
		loc.getWorld().spawnParticle(particle, loc, 0, (double) color.getRed() / 255, (double) color.getGreen() / 255, (double) color.getBlue() / 255, 0);
	}

	@Override
	public void spawnParticle(Particle particle, Location loc, int amount, double x, double y, double z, double speed, Material material) {
		loc.getWorld().spawnParticle(particle, loc, amount, x, y, z, 0, new MaterialData(material));
	}

	@Override
	public String getName(Enchantment enchant) {
		return enchant.getName();
	}

	@Override
	public FurnaceRecipe getFurnaceRecipe(String path, ItemStack item, Material material, float exp, int cook) {
		try {
			return (FurnaceRecipe) Class.forName("org.bukkit.inventory.FurnaceRecipe").getConstructor(ItemStack.class, Material.class, Integer.TYPE, Integer.TYPE).newInstance(item, material, 0, (int) exp);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException exception) {
			exception.printStackTrace();
			return null;
		}
	}

	@Override
	public MMORayTraceResult rayTrace(Player player, Vector direction, double range, Predicate<Entity> option) {

		Location loc = player.getEyeLocation();
		Vec3D vec = new Vec3D(loc.getDirection().getX(), loc.getDirection().getY(), loc.getDirection().getZ());
		MovingObjectPosition block = ((CraftPlayer) player).getHandle().getBoundingBox().b(vec, new Vec3D(vec.x, vec.y, vec.z).add(range * vec.x, range * vec.y, range * vec.z));

		double d = block == null ? range : Math.sqrt(block.pos.distanceSquared(new Vec3D(loc.getX(), loc.getY(), loc.getZ())));
		Ray3D line = new Ray3D(player.getEyeLocation());
		for (Entity entity : player.getNearbyEntities(d, d, d))
			if (line.intersectsRay(((CraftEntity) entity).getHandle().getBoundingBox()) && option.test(entity))
				return new MMORayTraceResult((LivingEntity) entity, range);

		return new MMORayTraceResult(null, range);
	}

	public class Ray3D extends Vec3D {
		public final Vec3D dir;

		/*
		 * warning, direction is not normalized
		 */
		public Ray3D(Vec3D origin, Vec3D direction) {
			super(origin.x, origin.y, origin.z);
			dir = direction;
		}

		/**
		 * Construct a 3D ray from a location.
		 * 
		 * @param loc
		 *            - the Bukkit location.
		 */
		public Ray3D(Location loc) {
			this(new Vec3D(loc.getX(), loc.getY(), loc.getZ()), new Vec3D(loc.getDirection().getX(), loc.getDirection().getY(), loc.getDirection().getZ()));
		}

		public Vec3D getDirection() {
			return dir;
		}

		public String toString() {
			return "origin: " + super.toString() + " dir: " + dir;
		}

		/**
		 * Calculates intersection with the given ray between a certain distance
		 * interval.
		 * <p>
		 * Ray-box intersection is using IEEE numerical properties to ensure the
		 * test is both robust and efficient, as described in: <br>
		 * <code>Amy Williams, Steve Barrus, R. Keith Morley, and Peter Shirley: "An
		 * Efficient and Robust Ray-Box Intersection Algorithm" Journal of graphics
		 * tools, 10(1):49-54, 2005</code>
		 * 
		 * @param ray
		 *            incident ray
		 * @param minDist
		 * @param maxDist
		 * @return intersection point on the bounding box (only the first is
		 *         returned) or null if no intersection
		 */
		public boolean intersectsRay(AxisAlignedBB box) {
			Vec3D invDir = new Vec3D(1f / dir.x, 1f / dir.y, 1f / dir.z);

			Vec3D min = new Vec3D(box.a, box.b, box.c);
			Vec3D max = new Vec3D(box.d, box.e, box.f);

			boolean signDirX = invDir.x < 0;
			boolean signDirY = invDir.y < 0;
			boolean signDirZ = invDir.z < 0;

			Vec3D bbox = signDirX ? max : min;
			double tmin = (bbox.x - x) * invDir.x;
			bbox = signDirX ? min : max;
			double tmax = (bbox.x - x) * invDir.x;
			bbox = signDirY ? max : min;
			double tymin = (bbox.y - y) * invDir.y;
			bbox = signDirY ? min : max;
			double tymax = (bbox.y - y) * invDir.y;

			if ((tmin > tymax) || (tymin > tmax)) {
				return false;
			}
			if (tymin > tmin) {
				tmin = tymin;
			}
			if (tymax < tmax) {
				tmax = tymax;
			}

			bbox = signDirZ ? max : min;
			double tzmin = (bbox.z - z) * invDir.z;
			bbox = signDirZ ? min : max;
			double tzmax = (bbox.z - z) * invDir.z;

			if ((tmin > tzmax) || (tzmin > tmax)) {
				return false;
			}
			if (tzmin > tmin) {
				tmin = tzmin;
			}
			if (tzmax < tmax) {
				tmax = tzmax;
			}
			return true;
		}
	}

	@Override
	public void applyDurability(ItemStack item, ItemMeta meta, int damage) {
		item.setDurability((short) damage);
	}

	@Override
	public int getDurability(ItemStack item, ItemMeta meta) {
		return item.getDurability();
	}

	@Override
	public boolean isDamaged(ItemStack item, ItemMeta meta) {
		return item.getDurability() > 0;
	}

	@Override
	public void repair(ItemStack item, int amount) {
		item.setDurability((short) Math.max(0, item.getDurability() - amount));
	}

	@Override
	public ItemStack generate(Material material, int damage) {
		return new ItemStack(material, 1, (short) damage);
	}

	@Override
	public boolean isDamageable(ItemStack item) {
		return item.getType().getMaxDurability() > 20;
	}

	@Override
	public NBTItem copyTexture(NBTItem item) {
		return MMOLib.plugin.getNMS().getNBTItem(new ItemStack(item.getItem().getType(), 1, item.getItem().getDurability())).addTag(new ItemTag("Unbreakable", true));
	}

	@Override
	public ItemStack textureItem(Material material, int model) {
		return MMOLib.plugin.getNMS().getNBTItem(new ItemStack(material, 1, (short) model)).addTag(new ItemTag("Unbreakable", true)).toItem();
	}

	@Override
	public BossBar createBossBar(NamespacedKey key, String title, BarColor color, BarStyle style, BarFlag... flags) {
		return Bukkit.createBossBar(title, color, style, flags);
	}

	@Override
	public Enchantment getEnchantmentFromString(String s) {
		if (s.equals("protection"))
			s = "PROTECTION_ENVIRONMENTAL";
		if (s.equals("fire_protection"))
			s = "PROTECTION_FIRE";
		if (s.equals("feather_falling"))
			s = "PROTECTION_FALL";
		if (s.equals("blast_protection"))
			s = "PROTECTION_EXPLOSIONS";
		if (s.equals("projectile_protection"))
			s = "PROTECTION_PROJECTILE";
		if (s.equals("respiration"))
			s = "OXYGEN";
		if (s.equals("aqua_affinity"))
			s = "WATER_WORKER";
		if (s.equals("sharpness"))
			s = "DAMAGE_ALL";
		if (s.equals("smite"))
			s = "DAMAGE_UNDEAD";
		if (s.equals("bane_of_arthropods"))
			s = "DAMAGE_ARTHROPODS";
		if (s.equals("looting"))
			s = "LOOT_BONUS_MOBS";
		if (s.equals("sweeping"))
			s = "SWEEPING_EDGE";
		if (s.equals("efficiency"))
			s = "DIG_SPEED";
		if (s.equals("unbreaking"))
			s = "DURABILITY";
		if (s.equals("fortune"))
			s = "LOOT_BONUS_BLOCKS";
		if (s.equals("power"))
			s = "ARROW_DAMAGE";
		if (s.equals("punch"))
			s = "ARROW_KNOCKBACK";
		if (s.equals("flame"))
			s = "ARROW_FIRE";
		if (s.equals("infinity"))
			s = "ARROW_INFINITE";
		if (s.equals("luck_of_the_sea"))
			s = "LUCK";

		return Enchantment.getByName(s.toUpperCase());
	}

	@Override
	public FurnaceRecipe getFurnaceRecipe(NamespacedKey key, ItemStack item, Material material, float exp, int cook) {
		try {
			return (FurnaceRecipe) Class.forName("org.bukkit.inventory.FurnaceRecipe").getConstructor(ItemStack.class, Material.class, Integer.TYPE, Integer.TYPE).newInstance(item, material, 0, (int) exp);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException exception) {
			exception.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean isCropFullyGrown(Block block) {
		if (block.getState().getData() instanceof Crops) {
			Crops ageable = (Crops) block.getState().getData();
			return ageable.getState().equals(CropState.RIPE);
		}
		return false;
	}

	@Override
	public boolean isUndead(Entity entity) {
		EntityType type = entity.getType();
		return type == EntityType.SKELETON || type == EntityType.STRAY || type == EntityType.WITHER_SKELETON || type == EntityType.ZOMBIE || type == EntityType.HUSK || type == EntityType.PIG_ZOMBIE || type == EntityType.ZOMBIE_VILLAGER || type == EntityType.WITHER || type == EntityType.SKELETON_HORSE || type == EntityType.ZOMBIE_HORSE;
	}
}
