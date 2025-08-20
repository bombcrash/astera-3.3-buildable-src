package me.lyrica.modules.impl.movement;

import me.lyrica.events.SubscribeEvent;
import me.lyrica.events.impl.PlayerMoveEvent;
import me.lyrica.modules.Module;
import me.lyrica.modules.RegisterModule;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

@RegisterModule(name = "AVoid", description = "Prevents you from falling into the void.", category = Module.Category.MOVEMENT)
public class AntiVoidModule extends Module {
    @SubscribeEvent
    public void onPlayerMove(PlayerMoveEvent event) {
        if (mc.player == null || mc.world == null) return;

        if (falling()) {
            event.setCancelled(true);
            event.setMovement(new Vec3d(event.getMovement().x, 0, event.getMovement().z));

            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), true, mc.player.horizontalCollision));
        }
    }

    private boolean falling() {
        for (int i = (int) mc.player.getY(); i >= -64; i--) {
            if (!mc.world.isAir(BlockPos.ofFloored(mc.player.getX(), i, mc.player.getZ()))) {
                return false;
            }
        }

        return mc.player.fallDistance > 0;
    }
}
