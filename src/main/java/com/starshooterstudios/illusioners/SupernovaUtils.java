package com.starshooterstudios.illusioners;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class SupernovaUtils implements Listener {
    public static NamespacedKey customItemKey;

    public static void initialize(JavaPlugin plugin) {
        customItemKey = new NamespacedKey(plugin, "custom-item");

        Bukkit.getPluginManager().registerEvents(new SupernovaUtils(), plugin);
    }

    public static Entity getTrueDamager(Entity entity) {
        if (entity instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof Entity e) return e;
        }
        return entity;
    }

    public static ItemStack createItem(Material baseMaterial, MetaMaker metaMaker) {
        return createItem(baseMaterial, metaMaker, true);
    }

    public static ItemStack createItem(Material baseMaterial, MetaMaker metaMaker, boolean customItem) {
        ItemStack item = new ItemStack(baseMaterial);
        ItemMeta meta = item.getItemMeta();
        metaMaker.makeMeta(meta);
        if (customItem) meta.getPersistentDataContainer().set(customItemKey, PersistentDataType.BOOLEAN, true);
        item.setItemMeta(meta);
        return item;
    }

    public interface MetaMaker {
        void makeMeta(ItemMeta meta);
    }

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        for (ItemStack item : event.getInventory().getMatrix()) {
            if (item == null || item.getItemMeta() == null) continue;
            if (item.getPersistentDataContainer().has(customItemKey)) {
                event.getInventory().setResult(null);
            }
        }
    }
}
