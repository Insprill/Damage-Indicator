package com.zenya.damageindicator.event;

import com.zenya.damageindicator.scoreboard.HealthIndicator;
import com.zenya.damageindicator.storage.StorageFileManager;
import com.zenya.damageindicator.storage.ToggleManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Listeners implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageEvent(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof LivingEntity))
            return;
        if (StorageFileManager.getConfig().listContains("disabled-worlds", e.getEntity().getWorld().getName()))
            return;
        if (!StorageFileManager.getConfig().getBool("damage-indicators"))
            return;

        if (!(e.getEntity() instanceof Player) && StorageFileManager.getConfig().getBool("only-show-entity-damage-from-players")) {
            if (!(e instanceof EntityDamageByEntityEvent))
                return;
            EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent) e;
            if (ev.getDamager() instanceof Projectile) {
                if (!(((Projectile) ev.getDamager()).getShooter() instanceof Player))
                    return;
            } else if (!(((EntityDamageByEntityEvent) e).getDamager() instanceof Player)) {
                return;
            }
        }

        if (!StorageFileManager.getConfig().isAllowed("entity-type-list", e.getEntity().getType().name()))
            return;
        if (StorageFileManager.getConfig().listContains("ignored-entities", e.getEntity().getName()))
            return;

        Bukkit.getServer().getPluginManager().callEvent(new HologramSpawnEvent((LivingEntity) e.getEntity(), -e.getFinalDamage()));

        // Handle health indicator
        if (e.getEntity() instanceof Player) {
            HealthIndicator.INSTANCE.updateHealth((Player) e.getEntity());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityRegainHealthEvent(EntityRegainHealthEvent e) {
        if (!(e.getEntity() instanceof LivingEntity))
            return;
        if (e.getAmount() < 1) // Minecraft doesn't register heals of less than half a heart
            return;
        if (StorageFileManager.getConfig().listContains("disabled-worlds", e.getEntity().getWorld().getName()))
            return;
        if (!StorageFileManager.getConfig().getBool("heal-indicators"))
            return;
        if (!StorageFileManager.getConfig().isAllowed("entity-type-list", e.getEntity().getType().name()))
            return;
        if (StorageFileManager.getConfig().listContains("ignored-entities", e.getEntity().getName()))
            return;

        Bukkit.getServer().getPluginManager().callEvent(new HologramSpawnEvent((LivingEntity) e.getEntity(), e.getAmount()));

        // Handle health indicator
        if (e.getEntity() instanceof Player) {
            HealthIndicator.INSTANCE.updateHealth((Player) e.getEntity());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoinEvent(PlayerJoinEvent e) {
        HealthIndicator.INSTANCE.updateHealth(e.getPlayer());
        ToggleManager.INSTANCE.isToggled(e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoinEvent(PlayerQuitEvent e) {
        ToggleManager.INSTANCE.uncacheToggle(e.getPlayer().getUniqueId());
    }

}
