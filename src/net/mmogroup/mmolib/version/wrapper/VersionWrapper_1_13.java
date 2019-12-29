package net.mmogroup.mmolib.version.wrapper;

import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.MMORayTraceResult;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.item.NBTItem;

public class VersionWrapper_1_13 implements VersionWrapper {

	@Override
	public void spawnParticle(Particle particle, Location loc, int amount, double x, double y, double z, double speed, float size, Color color) {
		loc.getWorld().spawnParticle(particle, loc, amount, x, y, z, speed, new Particle.DustOptions(color, size));
	}

	@Override
	public void spawnParticle(Particle particle, Location loc, int amount, double x, double y, double z, double speed, Material material) {
		loc.getWorld().spawnParticle(particle, loc, amount, x, y, z, 0, material.createBlockData());
	}

	@Override
	public String getName(Enchantment enchant) {
		return enchant.getKey().getKey();
	}

	@Override
	public FurnaceRecipe getFurnaceRecipe(String path, ItemStack item, Material material, float exp, int cook) {
		return new FurnaceRecipe(new NamespacedKey(MMOLib.plugin, "mmoitems_furnace_" + path), item, material, exp, cook);
	}

	@Override
	public MMORayTraceResult rayTrace(Player player, Vector direction, double range, Predicate<Entity> option) {
		RayTraceResult hit = player.getWorld().rayTraceEntities(player.getEyeLocation(), direction, range, option);
		return new MMORayTraceResult(hit != null ? (LivingEntity) hit.getHitEntity() : null, hit != null ? hit.getHitPosition().distance(player.getEyeLocation().toVector()) : range);
	}

	@Override
	public void applyDurability(ItemStack item, ItemMeta meta, int damage) {
		if (meta instanceof Damageable)
			((Damageable) meta).setDamage(damage);
	}

	@Override
	public int getDurability(ItemStack item, ItemMeta meta) {
		return item.getItemMeta() instanceof Damageable ? ((Damageable) item.getItemMeta()).getDamage() : 0;
	}

	@Override
	public boolean isDamaged(ItemStack item, ItemMeta meta) {
		return meta instanceof Damageable && ((Damageable) meta).getDamage() > 0;
	}

	@Override
	public void repair(ItemStack item, int amount) {
		ItemMeta meta = item.getItemMeta();
		((Damageable) meta).setDamage(Math.max(0, ((Damageable) meta).getDamage() - amount));
		item.setItemMeta(meta);
	}

	@Override
	public ItemStack generate(Material material, int damage) {
		ItemStack item = new ItemStack(material);
		ItemMeta meta = item.getItemMeta();
		((Damageable) meta).setDamage(damage);
		item.setItemMeta(meta);
		return item;
	}

	@Override
	public boolean isDamageable(ItemStack item) {
		return item.hasItemMeta() && item.getItemMeta() instanceof Damageable;
	}

	@Override
	public NBTItem copyTexture(NBTItem item) {
		return MMOLib.plugin.getNMS().getNBTItem(new ItemStack(item.getItem().getType())).addTag(new ItemTag("Damage", item.getInteger("Damage")));
	}

	@Override
	public ItemStack textureItem(Material material, int model) {
		return MMOLib.plugin.getNMS().getNBTItem(new ItemStack(material)).addTag(new ItemTag("Damage", model)).toItem();
	}

	@Override
	public Enchantment getEnchantmentFromString(String s) {
		return Enchantment.getByKey(NamespacedKey.minecraft(s));
	}

	@Override
	public FurnaceRecipe getFurnaceRecipe(NamespacedKey key, ItemStack item, Material material, float exp, int cook) {
		return new FurnaceRecipe(key, item, material, exp, cook);
	}

	@Override
	public BossBar createBossBar(NamespacedKey key, String title, BarColor color, BarStyle style, BarFlag... flags) {
		return Bukkit.createBossBar(key, title, color, style, flags);
	}

	@Override
	public boolean isCropFullyGrown(Block block) {
		if (block.getBlockData() instanceof Ageable) {
			Ageable ageable = (Ageable) block.getBlockData();
			return ageable.getAge() == ageable.getMaximumAge();
		}
		return false;
	}

	@Override
	public boolean isUndead(Entity entity) {
		EntityType type = entity.getType();
		return type == EntityType.SKELETON || type == EntityType.STRAY || type == EntityType.WITHER_SKELETON || type == EntityType.ZOMBIE || type == EntityType.DROWNED || type == EntityType.HUSK || type == EntityType.PIG_ZOMBIE || type == EntityType.ZOMBIE_VILLAGER || type == EntityType.PHANTOM || type == EntityType.WITHER || type == EntityType.SKELETON_HORSE || type == EntityType.ZOMBIE_HORSE;
	}
}
