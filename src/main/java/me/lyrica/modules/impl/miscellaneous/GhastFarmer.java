package me.lyrica.modules.impl.miscellaneous;

import me.lyrica.modules.Module;
import me.lyrica.modules.RegisterModule;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.item.Items;

import java.util.LinkedList;
import java.util.Queue;

@RegisterModule(name = "GhastFarmer", description = "Farms ghast tears.", category = Module.Category.MISCELLANEOUS)
public class GhastFarmer extends Module {

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final Queue<Target> targets = new LinkedList<>();
    private boolean returningToStart = false;

    private int startX;
    private int startY;
    private int startZ;

    @Override
    public void onEnable() {
        if (mc.player != null && mc.world != null) {
            startX = (int) mc.player.getX();
            startY = (int) mc.player.getY();
            startZ = (int) mc.player.getZ();
            returningToStart = false;
            targets.clear();
            ClientTickEvents.END_CLIENT_TICK.register(client -> onUpdate());
        }
    }

    @Override
    public void onDisable() {
        if (mc.player != null && mc.world != null) {
            sendChatMessage("#stop");
        }
    }
    public void onUpdate() {
        if (mc.player == null || mc.world == null) return;

        if (!targets.isEmpty() && isPlayerAtTarget(targets.peek())) {
            targets.poll();
            if (!targets.isEmpty()) {
                sendChatMessage("#goto " + targets.peek().x + " " + targets.peek().y + " " + targets.peek().z);
            } else if (!returningToStart) {
                returningToStart = true;
                sendChatMessage("#goto " + startX + " " + startY + " " + startZ);
            }
            return;
        }
        if (targets.isEmpty() && !returningToStart) {
            findTargets();
        }
    }

    private void findTargets() {
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof GhastEntity) {
                targets.add(new Target((int) entity.getX(), (int) entity.getY(), (int) entity.getZ()));
            }
        }
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof ItemEntity && ((ItemEntity) entity).getStack().getItem() == Items.GHAST_TEAR) {
                targets.add(new Target((int) entity.getX(), (int) entity.getY(), (int) entity.getZ()));
            }
        }
        if (!targets.isEmpty()) {
            Target firstTarget = targets.peek();
            sendChatMessage("#goto " + firstTarget.x + " " + firstTarget.y + " " + firstTarget.z);
        }
    }

    private boolean isPlayerAtTarget(Target target) {
        return Math.abs(mc.player.getX() - target.x) < 1
                && Math.abs(mc.player.getY() - target.y) < 1
                && Math.abs(mc.player.getZ() - target.z) < 1;
    }

    private void sendChatMessage(String message) {
        if (mc.player != null && mc.getNetworkHandler() != null) {
            mc.player.networkHandler.sendChatMessage(message);
        }
    }

    private static class Target {
        int x, y, z;

        Target(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}