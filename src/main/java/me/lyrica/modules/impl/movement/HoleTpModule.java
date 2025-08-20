package me.lyrica.modules.impl.movement;

import me.lyrica.Lyrica;
import me.lyrica.events.SubscribeEvent;
import me.lyrica.events.impl.PacketSendEvent;
import me.lyrica.modules.Module;
import me.lyrica.modules.RegisterModule;
import me.lyrica.settings.impl.BooleanSetting;
import me.lyrica.settings.impl.NumberSetting;
import me.lyrica.utils.minecraft.HoleUtils;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@RegisterModule(name = "HoleTp", description = "Teleports you to the nearest uncovered hole.", category = Module.Category.MOVEMENT)
public class HoleTpModule extends Module {
    
    public NumberSetting range = new NumberSetting("Range", "Maximum search range for holes.", 20, 1, 50);
    public NumberSetting maxHeight = new NumberSetting("MaxHeight", "Maximum height difference to consider.", 5, 1, 10);
    public BooleanSetting simulateStep = new BooleanSetting("SimulateStep", "Simulates step over blocks in your path", false);
    public BooleanSetting doubleHoles = new BooleanSetting("DoubleHoles", "Allows teleporting to 2-block wide holes", true);
    public BooleanSetting useNoClip = new BooleanSetting("UseNoClip", "Uses NoClip-like technique to bypass anti-cheat", true);
    public NumberSetting clipSteps = new NumberSetting("ClipSteps", "Number of teleport steps when using NoClip", 10, 5, 25);
    public NumberSetting packetDelay = new NumberSetting("PacketDelay", "Delay between teleport packets (ms)", 2, 0, 10);
    public BooleanSetting setOnGround = new BooleanSetting("SetOnGround", "Set onGround to false during teleport", true);
    
    private final Queue<Packet<?>> packetQueue = new ConcurrentLinkedQueue<>();
    private boolean teleporting = false;
    
    @Override
    public void onEnable() {
        if (getNull()) return;
        
        List<HoleUtils.Hole> holes = findSuitableHoles();
        
        if (holes.isEmpty()) {
            if (Lyrica.CHAT_MANAGER != null) {
                Lyrica.CHAT_MANAGER.message("No suitable holes found!");
            }
            setToggled(false);
            return;
        }
        
        // Get the nearest suitable hole
        HoleUtils.Hole targetHole = holes.get(0);
        Box holeBox = targetHole.box();
        
        // Teleport directly to hole
        Vec3d targetPos = new Vec3d(holeBox.getCenter().x, holeBox.minY, holeBox.getCenter().z);

        // Check if there's an obstacle in the way
        boolean hasObstacle = checkForObstacle(targetPos);

        if (useNoClip.getValue() && hasObstacle) {
            // NoClip benzeri teknik ile teleport
            executeNoClipTeleport(targetPos);
        } else if (simulateStep.getValue() && hasObstacle) {
            // Eski step simülasyonu
            simulateStepMotion(targetPos);
        } else {
            // Direkt teleport
            mc.player.setPosition(targetPos.x, targetPos.y, targetPos.z);
        }
        
        // Disable the module after teleporting
        setToggled(false);
    }
    
    @SubscribeEvent
    public void onPacketSend(PacketSendEvent event) {
        if (!isToggled() || !teleporting) return;
        
        // Oyuncu hareket paketlerini yakala ve iptal et
        if (event.getPacket() instanceof PlayerMoveC2SPacket) {
            event.setCancelled(true);
        }
    }
    
    private void executeNoClipTeleport(Vec3d targetPos) {
        // NoClip benzeri teleport başlat
        teleporting = true;
        packetQueue.clear();
        
        Vec3d startPos = mc.player.getPos();
        int steps = clipSteps.getValue().intValue();
        
        // Öncelikle yükselen bir paket gönder (yerçekimini manipüle etmek için)
        sendPositionPacket(startPos.x, startPos.y + 0.05, startPos.z, false);
        
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Adım adım hedefe doğru teleport et
        for (int i = 0; i <= steps; i++) {
            double progress = (double) i / steps;
            
            // Easing function - Daha doğal bir hareket için
            double easeProgress = easeInOutQuad(progress);
            
            double x = startPos.x + (targetPos.x - startPos.x) * easeProgress;
            double y = startPos.y + (targetPos.y - startPos.y) * easeProgress;
            double z = startPos.z + (targetPos.z - startPos.z) * easeProgress;
            
            // Her adımda bir pozisyon paketi gönder
            boolean isOnGround = !setOnGround.getValue() || i == steps;
            sendPositionPacket(x, y, z, isOnGround);
            
            // Her adımda client pozisyonunu güncelle
            mc.player.setPosition(x, y, z);
            
            try {
                Thread.sleep(packetDelay.getValue().longValue());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        // Son olarak, bir final teleport paketi gönder (hedefe tam olarak yerleştir)
        sendPositionPacket(targetPos.x, targetPos.y, targetPos.z, true);
        
        // Client pozisyonunu güncelle
        mc.player.setPosition(targetPos.x, targetPos.y, targetPos.z);
        mc.player.setVelocity(0, 0, 0);
        
        // Teleport bitir
        teleporting = false;
    }
    
    // Easing function - Daha doğal hareket için
    private double easeInOutQuad(double x) {
        return x < 0.5 ? 2 * x * x : 1 - Math.pow(-2 * x + 2, 2) / 2;
    }
    
    private void sendPositionPacket(double x, double y, double z, boolean onGround) {
        PlayerMoveC2SPacket packet = new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, onGround, false);
        mc.getNetworkHandler().sendPacket(packet);
    }

    private boolean checkForObstacle(Vec3d targetPos) {
        // Hedefe giderken oyuncunun yolunda engel var mı kontrol et
        Vec3d playerPos = mc.player.getPos();
        Vec3d direction = targetPos.subtract(playerPos).normalize();
        
        // Önündeki blokları kontrol et
        for (int i = 1; i <= 3; i++) {
            double checkDistance = i * 0.9; // Her adımda kontrol mesafesi
            if (checkDistance > playerPos.distanceTo(targetPos)) {
                break; // Hedefin ötesine geçmeyelim
            }
            
            Vec3d checkPos = playerPos.add(direction.multiply(checkDistance));
            BlockPos blockPos = new BlockPos((int)Math.floor(checkPos.x), (int)Math.floor(playerPos.y), (int)Math.floor(checkPos.z));
            BlockState blockState = mc.world.getBlockState(blockPos);
            
            // Eğer önündeki blok katıysa ve 1 blok yüksekliğinde bir engelse
            if (!blockState.isAir() && mc.world.getBlockState(blockPos.up()).isAir()) {
                return true;
            }
        }
        
        return false;
    }
    
    private void simulateStepMotion(Vec3d targetPos) {
        // Step hareketi simülasyonu
        // 1. Oyuncunun pozisyonunu al
        Vec3d playerPos = mc.player.getPos();
        
        // 2. Engelin üzerine çıkma hareketi için ilk paketler
        // Pre-jump ve ilerleme paketleri
        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                playerPos.x, playerPos.y + 0.42, playerPos.z, false, false));
        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                playerPos.x, playerPos.y + 0.75, playerPos.z, false, false));
        
        // 3. Engelin üzerine çıkıp ilerleme hareketi
        Vec3d direction = targetPos.subtract(playerPos).normalize();
        Vec3d midPoint = playerPos.add(direction.multiply(1.0)).add(0, 1.0, 0);
        
        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                midPoint.x, midPoint.y, midPoint.z, false, false));
        
        // 4. İnme hareketi
        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                midPoint.x, midPoint.y - 0.5, midPoint.z, false, false));
        
        // 5. Asıl hedefe teleport
        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                targetPos.x, targetPos.y, targetPos.z, false, false));
        
        // 6. Yere inme paketi
        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                targetPos.x, targetPos.y, targetPos.z, true, false));
    }
    
    private List<HoleUtils.Hole> findSuitableHoles() {
        List<HoleUtils.Hole> suitableHoles = new ArrayList<>();
        
        // Check if player is already in a hole
        BlockPos playerPos = mc.player.getBlockPos();
        boolean playerInHole = false;
        Box playerHoleBox = null;
        
        // Check if current position is a hole
        HoleUtils.Hole currentSingleHole = HoleUtils.getSingleHole(playerPos, 1);
        HoleUtils.Hole currentDoubleHole = doubleHoles.getValue() ? HoleUtils.getDoubleHole(playerPos, 1) : null;
        
        if (currentSingleHole != null) {
            playerInHole = true;
            playerHoleBox = currentSingleHole.box();
        } else if (currentDoubleHole != null) {
            playerInHole = true;
            playerHoleBox = currentDoubleHole.box();
        }
        
        for (int x = -range.getValue().intValue(); x <= range.getValue().intValue(); x++) {
            for (int z = -range.getValue().intValue(); z <= range.getValue().intValue(); z++) {
                for (int y = -maxHeight.getValue().intValue(); y <= maxHeight.getValue().intValue(); y++) {
                    BlockPos pos = mc.player.getBlockPos().add(x, y, z);
                    
                    // Check for single holes
                    HoleUtils.Hole singleHole = HoleUtils.getSingleHole(pos, 1);
                    if (singleHole != null) {
                        // Check if the hole is not covered (has air above)
                        BlockPos holePos = BlockPos.ofFloored(singleHole.box().getCenter().x, singleHole.box().minY, singleHole.box().getCenter().z);
                        if (mc.world.isAir(holePos.up()) && mc.world.isAir(holePos.up(2))) {
                            // Skip if this is the hole the player is already in
                            if (playerInHole && isSameHole(singleHole.box(), playerHoleBox)) {
                                continue;
                            }
                            suitableHoles.add(singleHole);
                        }
                        continue;
                    }
                    
                    // Check for double holes if enabled
                    if (doubleHoles.getValue()) {
                        HoleUtils.Hole doubleHole = HoleUtils.getDoubleHole(pos, 1);
                        if (doubleHole != null) {
                            // Check if the hole is not covered (has air above)
                            BlockPos holePos = BlockPos.ofFloored(doubleHole.box().getCenter().x, doubleHole.box().minY, doubleHole.box().getCenter().z);
                            if (mc.world.isAir(holePos.up()) && mc.world.isAir(holePos.up(2))) {
                                // Skip if this is the hole the player is already in
                                if (playerInHole && isSameHole(doubleHole.box(), playerHoleBox)) {
                                    continue;
                                }
                                suitableHoles.add(doubleHole);
                            }
                        }
                    }
                }
            }
        }
        
        // Sort by distance to player
        return suitableHoles.stream()
                .sorted(Comparator.comparing(h -> 
                    mc.player.squaredDistanceTo(
                        h.box().getCenter().x,
                        h.box().getCenter().y, 
                        h.box().getCenter().z)))
                .toList();
    }
    
    // Helper method to check if two holes are the same
    private boolean isSameHole(Box hole1, Box hole2) {
        return Math.abs(hole1.getCenter().x - hole2.getCenter().x) < 0.1 &&
               Math.abs(hole1.minY - hole2.minY) < 0.1 &&
               Math.abs(hole1.getCenter().z - hole2.getCenter().z) < 0.1;
    }
} 