package com.starshooterstudios.illusioners;

import io.papermc.paper.event.entity.EntityMoveEvent;
import io.papermc.paper.potion.PotionMix;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.raid.Raid;
import org.bukkit.*;
import org.bukkit.craftbukkit.CraftRaid;
import org.bukkit.craftbukkit.entity.CraftIllusioner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class Illusioners extends JavaPlugin implements Listener {
    private final NamespacedKey blindnessPotionKey = new NamespacedKey("illusioners", "blindness_potion");

    private NamespacedKey interactionKey;
    private NamespacedKey illusionerDataKey;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        illusionerSecondaryColour = getConfig().getBoolean("dungeons-illusioners") ? Color.PURPLE : Color.fromRGB(0, 0, 175);
        SupernovaUtils.initialize(this);
        initialize(this);

    }

    public void initialize(JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);

        illusionKey = new NamespacedKey(plugin, "illusioner_clone");
        illusionOwnerKey = new NamespacedKey(plugin, "illusioner_owner");
        illusionSeenByKey = new NamespacedKey(plugin, "illusioner_seen_by");
        illusionerDataKey = new NamespacedKey(plugin, "illusioner_data");
        interactionKey = new NamespacedKey(plugin, "illusioner_interaction");

        NamespacedKey longBlindnessPotionKey = new NamespacedKey(plugin, "long_blindness_potion");
        NamespacedKey shortBlindnessPotionKey = new NamespacedKey(plugin, "short_blindness_potion");

        shadowDust = SupernovaUtils.createItem(Material.PRISMARINE_CRYSTALS, meta -> {
            meta.setCustomModelData(1);
            meta.displayName(Component.text("Shadow Dust").decoration(TextDecoration.ITALIC, false));
            meta.getPersistentDataContainer().set(illusionKey, PersistentDataType.BOOLEAN, true);
        });

        popIllusionAdvancement = CustomAdvancements.makeAdvancement(
                new NamespacedKey(plugin, "pop_illusion"),
                "Not What He Seems",
                "Pop an Illusioner's illusion",
                shadowDust,
                CustomAdvancements.AdvancementFrame.GOAL,
                new CustomAdvancements.ParentedAdvancementData(Key.key("minecraft:adventure/voluntary_exile")),
                false,
                true,
                true,
                false
        );

        ItemStack shadowPotion = SupernovaUtils.createItem(Material.POTION, meta -> {
            meta.displayName(Component.text("Potion of Blindness").decoration(TextDecoration.ITALIC, false));
            meta.getPersistentDataContainer().set(blindnessPotionKey, PersistentDataType.BOOLEAN, true);
            meta.getPersistentDataContainer().set(shortBlindnessPotionKey, PersistentDataType.BOOLEAN, true);
            if (meta instanceof PotionMeta potionMeta) {
                potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.BLINDNESS, 900, 0, false, true, true), false);
            }
        });
        ItemStack splashShadowPotion = SupernovaUtils.createItem(Material.SPLASH_POTION, meta -> {
            meta.displayName(Component.text("Splash Potion of Blindness").decoration(TextDecoration.ITALIC, false));
            meta.getPersistentDataContainer().set(blindnessPotionKey, PersistentDataType.BOOLEAN, true);
            meta.getPersistentDataContainer().set(shortBlindnessPotionKey, PersistentDataType.BOOLEAN, true);
            if (meta instanceof PotionMeta potionMeta) {
                potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.BLINDNESS, 900, 0, false, true, true), false);
            }
        });
        ItemStack lingeringShadowPotion = SupernovaUtils.createItem(Material.LINGERING_POTION, meta -> {
            meta.displayName(Component.text("Lingering Potion of Blindness").decoration(TextDecoration.ITALIC, false));
            meta.getPersistentDataContainer().set(blindnessPotionKey, PersistentDataType.BOOLEAN, true);
            meta.getPersistentDataContainer().set(shortBlindnessPotionKey, PersistentDataType.BOOLEAN, true);
            if (meta instanceof PotionMeta potionMeta) {
                potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.BLINDNESS, 225, 0, false, true, true), false);
            }
        });
        ItemStack longShadowPotion = SupernovaUtils.createItem(Material.POTION, meta -> {
            meta.displayName(Component.text("Potion of Blindness").decoration(TextDecoration.ITALIC, false));
            meta.getPersistentDataContainer().set(blindnessPotionKey, PersistentDataType.BOOLEAN, true);
            meta.getPersistentDataContainer().set(longBlindnessPotionKey, PersistentDataType.BOOLEAN, true);
            if (meta instanceof PotionMeta potionMeta) {
                potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.BLINDNESS, 1800, 0, false, true, true), false);
            }
        });
        ItemStack longSplashShadowPotion = SupernovaUtils.createItem(Material.SPLASH_POTION, meta -> {
            meta.displayName(Component.text("Splash Potion of Blindness").decoration(TextDecoration.ITALIC, false));
            meta.getPersistentDataContainer().set(blindnessPotionKey, PersistentDataType.BOOLEAN, true);
            meta.getPersistentDataContainer().set(longBlindnessPotionKey, PersistentDataType.BOOLEAN, true);
            if (meta instanceof PotionMeta potionMeta) {
                potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.BLINDNESS, 1800, 0, false, true, true), false);
            }
        });
        ItemStack longLingeringShadowPotion = SupernovaUtils.createItem(Material.LINGERING_POTION, meta -> {
            meta.displayName(Component.text("Lingering Potion of Blindness").decoration(TextDecoration.ITALIC, false));
            meta.getPersistentDataContainer().set(blindnessPotionKey, PersistentDataType.BOOLEAN, true);
            meta.getPersistentDataContainer().set(longBlindnessPotionKey, PersistentDataType.BOOLEAN, true);
            if (meta instanceof PotionMeta potionMeta) {
                potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.BLINDNESS, 450, 0, false, true, true), false);
            }
        });

        MinecraftServer.getServer().potionBrewing().addPotionMix(new PotionMix(new NamespacedKey(plugin, "blindness_potion"), shadowPotion,
                PotionMix.createPredicateChoice(itemStack -> {
                    if (itemStack.getItemMeta() instanceof PotionMeta meta) {
                        return (PotionType.AWKWARD.equals(meta.getBasePotionType()) && itemStack.getType().equals(Material.POTION));
                    }
                    return false;
                }), PotionMix.createPredicateChoice(itemStack -> itemStack.getItemMeta().getPersistentDataContainer().has(illusionKey))));
        MinecraftServer.getServer().potionBrewing().addPotionMix(new PotionMix(new NamespacedKey(plugin, "splash_blindness_potion"), splashShadowPotion,
                PotionMix.createPredicateChoice(itemStack -> {
                    if (itemStack.getItemMeta() instanceof PotionMeta meta) {
                        return (PotionType.AWKWARD.equals(meta.getBasePotionType()) && itemStack.getType().equals(Material.SPLASH_POTION));
                    }
                    return false;
                }), PotionMix.createPredicateChoice(itemStack -> itemStack.getItemMeta().getPersistentDataContainer().has(illusionKey))));
        MinecraftServer.getServer().potionBrewing().addPotionMix(new PotionMix(new NamespacedKey(plugin, "lingering_blindness_potion"), lingeringShadowPotion,
                PotionMix.createPredicateChoice(itemStack -> {
                    if (itemStack.getItemMeta() instanceof PotionMeta meta) {
                        return (PotionType.AWKWARD.equals(meta.getBasePotionType()) && itemStack.getType().equals(Material.LINGERING_POTION));
                    }
                    return false;
                }), PotionMix.createPredicateChoice(itemStack -> itemStack.getItemMeta().getPersistentDataContainer().has(illusionKey))));
        MinecraftServer.getServer().potionBrewing().addPotionMix(new PotionMix(new NamespacedKey(plugin, "splash_blindness_potion_from_potion"), splashShadowPotion,
                PotionMix.createPredicateChoice(itemStack -> {
                    if (itemStack.getItemMeta() instanceof PotionMeta meta) {
                        return (meta.getPersistentDataContainer().has(shortBlindnessPotionKey) && itemStack.getType().equals(Material.POTION));
                    }
                    return false;
                }), new RecipeChoice.MaterialChoice(Material.GUNPOWDER)));
        MinecraftServer.getServer().potionBrewing().addPotionMix(new PotionMix(new NamespacedKey(plugin, "lingering_blindness_potion_from_splash"), lingeringShadowPotion,
                PotionMix.createPredicateChoice(itemStack -> {
                    if (itemStack.getItemMeta() instanceof PotionMeta meta) {
                        return (meta.getPersistentDataContainer().has(shortBlindnessPotionKey) && itemStack.getType().equals(Material.SPLASH_POTION));
                    }
                    return false;
                }), new RecipeChoice.MaterialChoice(Material.DRAGON_BREATH)));
        MinecraftServer.getServer().potionBrewing().addPotionMix(new PotionMix(new NamespacedKey(plugin, "long_splash_blindness_potion_from_potion"), longSplashShadowPotion,
                PotionMix.createPredicateChoice(itemStack -> {
                    if (itemStack.getItemMeta() instanceof PotionMeta meta) {
                        return (meta.getPersistentDataContainer().has(longBlindnessPotionKey) && itemStack.getType().equals(Material.POTION));
                    }
                    return false;
                }), new RecipeChoice.MaterialChoice(Material.GUNPOWDER)));
        MinecraftServer.getServer().potionBrewing().addPotionMix(new PotionMix(new NamespacedKey(plugin, "long_lingering_blindness_potion_from_splash"), longLingeringShadowPotion,
                PotionMix.createPredicateChoice(itemStack -> {
                    if (itemStack.getItemMeta() instanceof PotionMeta meta) {
                        return (meta.getPersistentDataContainer().has(longBlindnessPotionKey) && itemStack.getType().equals(Material.SPLASH_POTION));
                    }
                    return false;
                }), new RecipeChoice.MaterialChoice(Material.DRAGON_BREATH)));

        MinecraftServer.getServer().potionBrewing().addPotionMix(new PotionMix(new NamespacedKey(plugin, "long_blindness_potion"), longShadowPotion,
                PotionMix.createPredicateChoice(itemStack -> {
                    if (itemStack.getItemMeta() instanceof PotionMeta meta) {
                        return (meta.getPersistentDataContainer().has(shortBlindnessPotionKey) && itemStack.getType().equals(Material.POTION));
                    }
                    return false;
                }), new RecipeChoice.MaterialChoice(Material.REDSTONE)));

        MinecraftServer.getServer().potionBrewing().addPotionMix(new PotionMix(new NamespacedKey(plugin, "long_splash_blindness_potion"), longSplashShadowPotion,
                PotionMix.createPredicateChoice(itemStack -> {
                    if (itemStack.getItemMeta() instanceof PotionMeta meta) {
                        return (meta.getPersistentDataContainer().has(shortBlindnessPotionKey) && itemStack.getType().equals(Material.SPLASH_POTION));
                    }
                    return false;
                }), new RecipeChoice.MaterialChoice(Material.REDSTONE)));

        MinecraftServer.getServer().potionBrewing().addPotionMix(new PotionMix(new NamespacedKey(plugin, "long_lingering_blindness_potion"), longLingeringShadowPotion,
                PotionMix.createPredicateChoice(itemStack -> {
                    if (itemStack.getItemMeta() instanceof PotionMeta meta) {
                        return (meta.getPersistentDataContainer().has(shortBlindnessPotionKey) && itemStack.getType().equals(Material.LINGERING_POTION));
                    }
                    return false;
                }), new RecipeChoice.MaterialChoice(Material.REDSTONE)));
    }

    private ItemStack shadowDust;
    private final Random random = new Random();

    private CustomAdvancements.Advancement popIllusionAdvancement;

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.getEntity() instanceof Evoker evoker) {
            if (random.nextBoolean()) return;
            Illusioner illusioner = (Illusioner) evoker.getWorld().spawnEntity(evoker.getLocation(), EntityType.ILLUSIONER, CreatureSpawnEvent.SpawnReason.RAID);
            illusioner.setPatrolLeader(evoker.isPatrolLeader());
            illusioner.setRaid(evoker.getRaid());
            if (evoker.getRaid() != null) {
                Raid raid = ((CraftRaid) evoker.getRaid()).getHandle();
                raid.addWaveMob(evoker.getWave(), ((CraftIllusioner) illusioner).getHandle(), false);
                if (illusioner.isPatrolLeader()) {
                    raid.setLeader(evoker.getWave(), ((CraftIllusioner) illusioner).getHandle());
                }
            }
            illusioner.setWave(evoker.getWave());
            illusioner.getEquipment().setHelmet(evoker.getEquipment().getHelmet());
            evoker.remove();
        } else if (event.getEntity() instanceof Illusioner illusioner) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
                if (illusioner.getPersistentDataContainer().has(illusionKey)) return;
                setupInteraction(illusioner);
                setVisibility(illusioner, true);
            });
        }
    }

    public List<Illusioner> getIllusions(Entity entity) {
        List<Illusioner> entities = new ArrayList<>();
        PersistentDataContainer container = entity.getPersistentDataContainer().getOrDefault(illusionOwnerKey, PersistentDataType.TAG_CONTAINER, entity.getPersistentDataContainer().getAdapterContext().newPersistentDataContainer());
        for (NamespacedKey s : container.getKeys()) {
            Entity e = Bukkit.getEntity(UUID.fromString(s.getKey()));
            if (e == null || e.isDead()) container.remove(s);
            else entities.add((Illusioner) e);
        }
        entity.getPersistentDataContainer().set(illusionOwnerKey, PersistentDataType.TAG_CONTAINER, container);
        return entities;
    }

    @EventHandler
    public void onEntityMove(EntityMoveEvent event) {
        if (event.getEntity().getPersistentDataContainer().has(illusionKey)) {
            checkDistance(event.getEntity());
        } else if (event.getEntity().getPersistentDataContainer().has(illusionOwnerKey)) {
            for (Entity entity : getIllusions(event.getEntity())) {
                checkDistance(entity);
            }
        }

        Interaction i = getInteraction(event.getEntity());
        if (i != null) {
            i.teleport(event.getEntity());
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getPersistentDataContainer().has(illusionOwnerKey)) {
            for (Entity entity : getIllusions(event.getEntity())) {
                popIllusion(entity, 0);
            }
        }
        Interaction interaction = getInteraction(event.getEntity());
        if (interaction != null) {
            interaction.remove();
        }
        if (event.getEntity().getType().equals(EntityType.ILLUSIONER)) {
            int i = 2;
            if (event.getEntity().getKiller() != null) {
                i *= event.getEntity().getKiller().getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOTING)+1;
            }
            int amount = random.nextInt(0, i+1);
            if (amount == 0) return;
            ItemStack dust = shadowDust.clone();
            dust.setAmount(amount);
            event.getDrops().add(dust);
        }
    }

    public Interaction getInteraction(Entity entity) {
        String s = entity.getPersistentDataContainer().get(illusionerDataKey, PersistentDataType.STRING);
        if (s == null) return null;
        UUID u = UUID.fromString(s);
        return (Interaction) Bukkit.getEntity(u);
    }

    public Illusioner getIllusioner(Entity entity) {
        String s = entity.getPersistentDataContainer().get(interactionKey, PersistentDataType.STRING);
        if (s == null) return null;
        UUID u = UUID.fromString(s);
        return (Illusioner) Bukkit.getEntity(u);
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getEntity().getPersistentDataContainer().has(illusionOwnerKey)) {
            if (event.getTarget() instanceof LivingEntity e) for (Illusioner entity : getIllusions(event.getEntity())) entity.setTarget(e);
        }
        if (event.getTarget() == null) return;
        if (event.getTarget().getPersistentDataContainer().has(illusionOwnerKey)) {
            if (!wasSeenBy(event.getTarget(), event.getEntity())) {
                List<Illusioner> illusions = getIllusions(event.getTarget());
                if (illusions.isEmpty()) return;
                Illusioner newTarget = illusions.get(random.nextInt(illusions.size()));
                event.setTarget(newTarget);
            }
        }
    }

    public void addSeenBy(Entity illusioner, Entity saw) {
        if (random.nextDouble() > 0.75) return;
        PersistentDataContainer container = illusioner.getPersistentDataContainer().getOrDefault(illusionSeenByKey, PersistentDataType.TAG_CONTAINER, illusioner.getPersistentDataContainer().getAdapterContext().newPersistentDataContainer());
        container.set(new NamespacedKey(this, saw.getUniqueId().toString()), PersistentDataType.BOOLEAN, true);
        illusioner.getPersistentDataContainer().set(illusionSeenByKey, PersistentDataType.TAG_CONTAINER, container);
    }

    public boolean wasSeenBy(Entity entity, Entity saw) {
        PersistentDataContainer container = entity.getPersistentDataContainer().getOrDefault(illusionSeenByKey, PersistentDataType.TAG_CONTAINER, entity.getPersistentDataContainer().getAdapterContext().newPersistentDataContainer());
        for (NamespacedKey key : container.getKeys()) {
            if (saw.getUniqueId().toString().equalsIgnoreCase(key.getKey())) return true;
        }
        return false;
    }

    @EventHandler
    @SuppressWarnings("UnstableApiUsage")
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Interaction interaction) {
            Illusioner i = getIllusioner(interaction);
            if (i != null) {
                setVisibility(i, true);
                i.damage(event.getDamage(), event.getDamageSource());
            }
        }
        if (event.getEntity().getPersistentDataContainer().has(illusionOwnerKey)) {
            for (Entity entity : getIllusions(event.getEntity())) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
                    if (event.getEntity().isDead()) return;
                    popIllusion(entity, 4);
                });
            }
        }
        if (event.getEntity().getPersistentDataContainer().has(illusionKey)) {
            if (event instanceof EntityDamageByEntityEvent damageByEntityEvent) {
                Entity damager = SupernovaUtils.getTrueDamager(damageByEntityEvent.getDamager());
                if (damager.getType().equals(EntityType.ILLUSIONER)) {
                    event.setCancelled(true);
                    return;
                } else if (damager instanceof Player player) popIllusionAdvancement.grant(player);
                Entity source = getIllusionSource(event.getEntity());
                if (source != null) addSeenBy(source, damager);
            }
            event.setCancelled(true);
            popIllusion(event.getEntity(), 1);
        }
    }

    public void checkDistance(Entity entity) {
        Entity source = getIllusionSource(entity);
        if (source == null) popIllusion(entity, 0);
        else if (isOutOfRange(entity, source)) popIllusion(entity, 0);
    }

    public @Nullable Entity getIllusionSource(Entity entity) {
        String s = entity.getPersistentDataContainer().get(illusionKey, PersistentDataType.STRING);
        if (s == null) return null;
        return Bukkit.getEntity(UUID.fromString(s));
    }

    public boolean isOutOfRange(Entity entity, Entity source) {
        if (!entity.getWorld().equals(source.getWorld())) return true;
        return entity.getLocation().distance(source.getLocation()) > 16;
    }

    @EventHandler
    public void onEntityPotionEffect(EntityPotionEffectEvent event) {
        if (event.getNewEffect() == null) return;
        if (!event.getNewEffect().getType().equals(PotionEffectType.INVISIBILITY)) return;
        if (event.getEntityType().equals(EntityType.ILLUSIONER)) {
            event.setCancelled(true);
        }
    }

    private NamespacedKey illusionKey;
    private NamespacedKey illusionOwnerKey;
    private NamespacedKey illusionSeenByKey;
    private Color illusionerSecondaryColour;

    @EventHandler
    public void onEntitySpellCast(EntitySpellCastEvent event) {
        if (event.getEntity().getPersistentDataContainer().has(illusionKey)) {
            event.setCancelled(true);
            return;
        }
        if (event.getSpell().equals(Spellcaster.Spell.DISAPPEAR)) {
            event.setCancelled(true);
            List<Illusioner> illusions = getIllusions(event.getEntity());
            if (illusions.size() >= 6) return;
            PersistentDataContainer container = event.getEntity().getPersistentDataContainer().getOrDefault(illusionOwnerKey, PersistentDataType.TAG_CONTAINER, event.getEntity().getPersistentDataContainer().getAdapterContext().newPersistentDataContainer());
            int amount = 6 - illusions.size();
            if (amount <= 0) return;
            for (int i = 0; i < amount; i++) {
                Illusioner il = (Illusioner) event.getEntity().getWorld().spawnEntity(event.getEntity().getLocation(), EntityType.ILLUSIONER, CreatureSpawnEvent.SpawnReason.SPELL);
                il.setPatrolLeader(false);
                il.setCanJoinRaid(false);
                il.getEquipment().setHelmet(null);
                il.getPersistentDataContainer().set(illusionKey, PersistentDataType.STRING, event.getEntity().getUniqueId().toString());
                ((CraftIllusioner) il).getHandle().removeAllGoals(goal -> goal.toString().toLowerCase().contains("spell"));
                container.set(new NamespacedKey(this, il.getUniqueId().toString()), PersistentDataType.BOOLEAN, true);
            }
            event.getEntity().getPersistentDataContainer().set(illusionOwnerKey, PersistentDataType.TAG_CONTAINER, container);
            setVisibility(event.getEntity(), false);
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Illusioner illusioner) {
            if (illusioner.getPersistentDataContainer().has(illusionKey)) event.getEntity().getPersistentDataContainer().set(illusionKey, PersistentDataType.BOOLEAN, true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity().getPersistentDataContainer().has(illusionKey)) {
            event.setCancelled(true);
            event.getEntity().getWorld().spawnParticle(Particle.DUST, event.getEntity().getLocation(), 6, 0.1, 0.1, 0.1, new Particle.DustOptions(Color.GRAY, 1));
            event.getEntity().remove();
        }
        if (event.getHitEntity() != null) {
            if (event.getHitEntity().getPersistentDataContainer().has(illusionKey)) {
                event.setCancelled(true);
                if (event.getEntity().getShooter() instanceof Illusioner) return;
                popIllusion(event.getHitEntity(), 1);
            }
        }
    }

    public void popIllusion(Entity entity, float trailSpeed) {
        Location loc = entity.getLocation().add(0, 1, 0);
        entity.getWorld().spawnParticle(Particle.DUST, loc, 12, 0.4, 0.4, 0.4, new Particle.DustOptions(Color.GRAY, 1));
        entity.getWorld().spawnParticle(Particle.DUST, loc, 12, 0.3, 0.5, 0.3, new Particle.DustOptions(illusionerSecondaryColour, 1));
        Entity source = getIllusionSource(entity);
        if (source != null) {
            removeIllusion(source, entity);
            if (trailSpeed != 0) {
                int tick = Bukkit.getCurrentTick();
                final float[] size = {1};
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        size[0] = Math.max(0.1f, size[0]-0.01f*trailSpeed);
                        Location target = source.getLocation().clone().add(0, 1, 0);
                        loc.add(target.clone().subtract(loc).toVector().divide(new Vector(10/trailSpeed, 10/trailSpeed, 10/trailSpeed)));
                        loc.getWorld().spawnParticle(Particle.DUST, loc, 3, 0.1, 0.1, 0.1, new Particle.DustOptions(Color.GRAY, size[0]));
                        loc.getWorld().spawnParticle(Particle.DUST, loc, 3, 0.1, 0.1, 0.1, new Particle.DustOptions(illusionerSecondaryColour, size[0]));
                        if (loc.distance(target) < 0.6) cancel();
                        if (Bukkit.getCurrentTick() - tick > 400) cancel();
                    }
                }.runTaskTimer(this, 0, 1);
            }
        }
        entity.remove();
    }

    public void removeIllusion(Entity source, Entity entity) {
        PersistentDataContainer container = source.getPersistentDataContainer().getOrDefault(illusionOwnerKey, PersistentDataType.TAG_CONTAINER, source.getPersistentDataContainer().getAdapterContext().newPersistentDataContainer());
        container.remove(new NamespacedKey(this, entity.getUniqueId().toString()));
        source.getPersistentDataContainer().set(illusionOwnerKey, PersistentDataType.TAG_CONTAINER, container);
    }

    @EventHandler
    public void onEntitiesLoad(EntitiesLoadEvent event) {
        for (Entity entity : event.getEntities()) {
            if (entity.getPersistentDataContainer().has(illusionKey)) entity.remove();
            if (entity.getPersistentDataContainer().has(interactionKey)) entity.remove();
            if (entity.getPersistentDataContainer().has(illusionerDataKey)) {
                setupInteraction(entity);
            }
        }
    }

    public void setupInteraction(Entity entity) {
        Interaction interaction = (Interaction) entity.getWorld().spawnEntity(entity.getLocation(), EntityType.INTERACTION);
        interaction.setInteractionHeight(1.95f);
        interaction.setInteractionWidth(0.6f);
        entity.getPersistentDataContainer().set(illusionerDataKey, PersistentDataType.STRING, interaction.getUniqueId().toString());
        interaction.getPersistentDataContainer().set(interactionKey, PersistentDataType.STRING, entity.getUniqueId().toString());
        interaction.setInvisible(true);
    }

    public void setVisibility(Entity entity, boolean visible) {
        entity.setVisibleByDefault(visible);
        Interaction i = getInteraction(entity);
        if (i != null) i.setVisibleByDefault(!visible);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) throws ExecutionException, InterruptedException {
        if (!getConfig().getBoolean("enable-resource-pack")) return;
        ResourcePackInfo packInfo = ResourcePackInfo.resourcePackInfo()
                .uri(URI.create("https://github.com/cometcake575/Illusioners/raw/refs/heads/main/IllusionerPack-%s.zip".formatted(getConfig().getBoolean("dungeons-illusioners") ? "Dungeons" : "Default")))
                .computeHashAndBuild().get();
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> event.getPlayer().sendResourcePacks(packInfo), 5);
    }
}
