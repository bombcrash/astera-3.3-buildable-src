package me.lyrica.modules.impl.combat;

import me.lyrica.Lyrica;
import me.lyrica.events.SubscribeEvent;
import me.lyrica.events.impl.EntitySpawnEvent;
import me.lyrica.events.impl.TickEvent;
import me.lyrica.modules.Module;
import me.lyrica.modules.RegisterModule;
import me.lyrica.settings.impl.BooleanSetting;
import me.lyrica.settings.impl.ModeSetting;
import me.lyrica.settings.impl.NumberSetting;
import me.lyrica.utils.minecraft.InventoryUtils;
import me.lyrica.utils.minecraft.WorldUtils;
import me.lyrica.utils.rotations.RotationUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;

@RegisterModule(name = "PearlBlocker", description = "Blocks ender pearls in flight and preemptively blocks players about to throw pearls.", category = Module.Category.COMBAT)
public class PearlBlocker extends Module {
    // Settings
    public BooleanSetting ignoreFriends = new BooleanSetting("IgnoreFriends", "Don't block friends' pearls", true);
    public NumberSetting range = new NumberSetting("Range", "Detection range", 10.0, 1.0, 30.0);
    public BooleanSetting obsidianOnly = new BooleanSetting("ObsidianOnly", "Only use obsidian", true);
    public BooleanSetting predictive = new BooleanSetting("Predictive", "Predict pearl throws from movements", true);
    public BooleanSetting debug = new BooleanSetting("Debug", "Show debug messages", false);
    public BooleanSetting silentRotation = new BooleanSetting("SilentRotation", "Use silent rotations to place blocks", true);
    public ModeSetting wallSize = new ModeSetting("WallSize", "Size of the wall to place in pearl's path", "3x3", new String[]{"3x3", "5x5"});
    public NumberSetting placeCooldown = new NumberSetting("PlaceCooldown", "Cooldown between block placements per player (ms)", 80.0, 50.0, 500.0);
    public NumberSetting rotateCooldown = new NumberSetting("RotateCooldown", "Cooldown between rotations (ms)", 60.0, 30.0, 500.0);
//push mesajı
    // Cooldowns to prevent spam and rotate desync
    private final Map<String, Long> lastPlaceTime = new HashMap<>(); // playerName -> last place ms
    private long lastRotateTime = 0;
    private float[] lastRotation = null;

    @Override
    public void onDisable() {
        lastPlaceTime.clear();
        lastRotateTime = 0;
        lastRotation = null;
    }

    // --- PEARL DETECTION ---
    @SubscribeEvent
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (getNull()) return;
        if (!(event.getEntity() instanceof EnderPearlEntity pearl)) return;
        Entity owner = pearl.getOwner();
        if (!(owner instanceof PlayerEntity player)) return;
        if (player == mc.player) return;
        if (ignoreFriends.getValue() && Lyrica.FRIEND_MANAGER.contains(player.getName().getString())) return;
        double dist = mc.player.squaredDistanceTo(pearl);
        if (dist > Math.pow(range.getValue().doubleValue(), 2)) return;
        String name = player.getName().getString();
        long now = System.currentTimeMillis();
        if (now - lastPlaceTime.getOrDefault(name, 0L) < placeCooldown.getValue().longValue()) return;
        if (debug.getValue()) Lyrica.CHAT_MANAGER.tagged("Blocking pearl in flight from: " + name, getName());
        if (blockPearlPath(pearl, player)) {
            lastPlaceTime.put(name, now);
        }
    }

    // --- PREDICTIVE BLOCKING ---
    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (getNull() || !predictive.getValue()) return;
        long now = System.currentTimeMillis();
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;
            if (ignoreFriends.getValue() && Lyrica.FRIEND_MANAGER.contains(player.getName().getString())) continue;
            double dist = mc.player.squaredDistanceTo(player);
            if (dist > Math.pow(range.getValue().doubleValue(), 2)) continue;
            if (isHoldingPearl(player) && isLikelyToThrow(player)) {
                String name = player.getName().getString();
                if (now - lastPlaceTime.getOrDefault(name, 0L) < placeCooldown.getValue().longValue()) continue;
                if (debug.getValue()) Lyrica.CHAT_MANAGER.tagged("Predictive block: " + name, getName());
                if (blockPredictive(player)) {
                    lastPlaceTime.put(name, now);
                }
            }
        }
    }

    // --- HELPERS ---
    private boolean isHoldingPearl(PlayerEntity player) {
        return player.getMainHandStack().getItem() == Items.ENDER_PEARL || player.getOffHandStack().getItem() == Items.ENDER_PEARL;
    }

    // Predictive: Oyuncu pearl atmaya çok yakın mı?
    private boolean isLikelyToThrow(PlayerEntity player) {
        // Yere bakmıyor, zıplıyor veya hızlı hareket ediyor ve elinde pearl var ise
        return player.getPitch() < -30 || !player.isOnGround() || player.getVelocity().lengthSquared() > 0.2;
    }

    // Predictive: Oyuncunun önüne hızlı ve geniş blok koy
    private boolean blockPredictive(PlayerEntity player) {
        boolean placed = false;
        Vec3d pos = player.getPos();
        Vec3d lookVec = player.getRotationVector().normalize();
        int wallRadius = wallSize.getValue().equals("5x5") ? 2 : 1;
        // Oyuncunun 2-4 blok önüne, seçilen duvar boyutunda blok koy
        for (int i = 2; i <= 4; i++) {
            Vec3d targetPos = pos.add(lookVec.multiply(i));
            BlockPos center = BlockPos.ofFloored(targetPos);
            for (int x = -wallRadius; x <= wallRadius; x++) {
                for (int y = 0; y <= 1; y++) {
                    for (int z = -wallRadius; z <= wallRadius; z++) {
                        BlockPos wallPos = center.add(x, y, z);
                        placed |= placeBlockSmart(wallPos);
                    }
                }
            }
        }
        return placed;
    }

    // Pearl'ın yoluna geniş duvar koy
    private boolean blockPearlPath(EnderPearlEntity pearl, PlayerEntity player) {
        boolean placed = false;
        Vec3d velocity = pearl.getVelocity();
        if (velocity.lengthSquared() < 0.1) return false;
        Vec3d normalized = velocity.normalize();
        Vec3d pos = pearl.getPos();
        int wallRadius = wallSize.getValue().equals("5x5") ? 2 : 1;
        // Pearl'ın 3-7 blok ilerisinde, seçilen duvar boyutunda blok koy
        for (int d = 3; d <= 7; d++) {
            Vec3d future = pos.add(normalized.multiply(d));
            BlockPos center = BlockPos.ofFloored(future);
            boolean anyPlaced = false;
            for (int x = -wallRadius; x <= wallRadius; x++) {
                for (int y = 0; y <= 1; y++) {
                    for (int z = -wallRadius; z <= wallRadius; z++) {
                        BlockPos wallPos = center.add(x, y, z);
                        anyPlaced |= placeBlockSmart(wallPos);
                    }
                }
            }
            if (anyPlaced) {
                placed = true;
                break; // Sadece ilk başarılı duvarı koy
            }
        }
        return placed;
    }

    private boolean canPlaceBlock(BlockPos pos) {
        if (mc.world == null || mc.player == null) return false;
        if (mc.player.getBlockPos().equals(pos) || mc.player.getBlockPos().up().equals(pos)) return false;
        return mc.world.getBlockState(pos).isReplaceable();
    }

    // Returns true if block placed
    private boolean placeBlockSmart(BlockPos pos) {
        if (!canPlaceBlock(pos)) return false;
        int slot = obsidianOnly.getValue() ? InventoryUtils.findHotbar(Items.OBSIDIAN) : InventoryUtils.findHardestBlock(0, 8);
        if (slot == -1) {
            if (debug.getValue()) Lyrica.CHAT_MANAGER.tagged("No blocks found in hotbar!", getName());
            return false;
        }
        int prevSlot = mc.player.getInventory().selectedSlot;
        InventoryUtils.switchSlot("Silent", slot, prevSlot);
        BlockPos neighbor = findNeighbor(pos);
        if (neighbor == null) {
            if (debug.getValue()) Lyrica.CHAT_MANAGER.tagged("No valid neighbor for block at " + pos, getName());
            InventoryUtils.switchBack("Silent", slot, prevSlot);
            return false;
        }
        Direction dir = getPlacementDirection(pos, neighbor);
        // Only rotate if enough time passed or rotation is different
        if (silentRotation.getValue()) {
            float[] rotations = RotationUtils.getRotations(Vec3d.ofCenter(pos));
            long now = System.currentTimeMillis();
            if (lastRotation == null || Math.abs(rotations[0] - lastRotation[0]) > 2.0f || Math.abs(rotations[1] - lastRotation[1]) > 2.0f || now - lastRotateTime > rotateCooldown.getValue().longValue()) {
                Lyrica.ROTATION_MANAGER.packetRotate(rotations);
                lastRotateTime = now;
                lastRotation = rotations;
            }
        }
        WorldUtils.placeBlock(pos, dir, Hand.MAIN_HAND, false, false, false);
        InventoryUtils.switchBack("Silent", slot, prevSlot);
        return true;
    }

    private BlockPos findNeighbor(BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockPos offset = pos.offset(dir);
            if (!mc.world.getBlockState(offset).isReplaceable()) return offset;
        }
        return null;
    }

    private Direction getPlacementDirection(BlockPos pos, BlockPos neighbor) {
        int x = pos.getX() - neighbor.getX();
        int y = pos.getY() - neighbor.getY();
        int z = pos.getZ() - neighbor.getZ();
        if (x != 0) return x > 0 ? Direction.WEST : Direction.EAST;
        if (y != 0) return y > 0 ? Direction.DOWN : Direction.UP;
        if (z != 0) return z > 0 ? Direction.NORTH : Direction.SOUTH;
        return Direction.UP;
    }
} 