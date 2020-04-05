package net.mmogroup.mmolib.version.wrapper;

import java.util.function.Predicate;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import net.mmogroup.mmolib.api.MMORayTraceResult;
import net.mmogroup.mmolib.api.item.NBTItem;

public interface VersionWrapper {
	default void spawnParticle(Particle particle, Location loc, Color color) {
		spawnParticle(particle, loc, 1, 0, 0, 0, 0, 1, color);
	}

	default void spawnParticle(Particle particle, Location loc, float size, Color color) {
		spawnParticle(particle, loc, 1, 0, 0, 0, 0, size, color);
	}

	default void spawnParticle(Particle particle, Location loc, Material material) {
		spawnParticle(particle, loc, 1, 0, 0, 0, 0, material);
	}

	void spawnParticle(Particle particle, Location loc, int amount, double x, double y, double z, double speed, float size, Color color);

	void spawnParticle(Particle particle, Location loc, int amount, double x, double y, double z, double speed, Material material);

	String getName(Enchantment enchant);

	FurnaceRecipe getFurnaceRecipe(String path, ItemStack item, Material material, float exp, int cook);

	default MMORayTraceResult rayTrace(Player player, double range, Predicate<Entity> predicate) {
		return rayTrace(player.getEyeLocation(), player.getEyeLocation().getDirection(), range, predicate);
	}

	default MMORayTraceResult rayTrace(Player player, Vector direction, double range, Predicate<Entity> predicate) {
		return rayTrace(player.getEyeLocation(), direction, range, predicate);
	}

	MMORayTraceResult rayTrace(Location loc, Vector direction, double range, Predicate<Entity> predicate);

	/*
	 * used by MMOItems to update an item's durability bar in 1.12. in 1.12 you
	 * need to update the item durability using the ItemStack instance, in 1.13+
	 * you need to manipulate the itemMeta
	 */
	public void applyDurabilityData(ItemStack item, ItemStack data);

	public void applyDurability(ItemStack item, ItemMeta meta, int damage);

	public int getDurability(ItemStack item, ItemMeta meta);

	public boolean isDamaged(ItemStack item, ItemMeta meta);

	public boolean isDamageable(ItemStack item);

	public void repair(ItemStack item, int amount);

	public ItemStack generate(Material material, int damage);

	public NBTItem copyTexture(NBTItem item);

	public ItemStack textureItem(Material material, int model);

	BossBar createBossBar(NamespacedKey key, String title, BarColor color, BarStyle style, BarFlag... flags);

	Enchantment getEnchantmentFromString(String s);

	FurnaceRecipe getFurnaceRecipe(NamespacedKey key, ItemStack item, Material material, float exp, int cook);

	boolean isCropFullyGrown(Block block);

	boolean isUndead(Entity entity);
}
