package me.lyrica.modules.impl.movement.elytrafly;

import me.lyrica.events.SubscribeEvent;
import me.lyrica.events.impl.PacketReceiveEvent;
import me.lyrica.events.impl.PlayerMoveEvent;
import me.lyrica.events.impl.PlayerTravelEvent;
import me.lyrica.events.impl.SendMovementEvent;
import me.lyrica.mixins.accessors.PlayerMoveC2SPacketAccessor;
import me.lyrica.modules.Module;
import me.lyrica.modules.RegisterModule;
import me.lyrica.settings.impl.BooleanSetting;
import me.lyrica.settings.impl.ModeSetting;
import me.lyrica.settings.impl.NumberSetting;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.util.math.Vec3d;

import java.util.List;

@RegisterModule(name = "ElytraFly", description = "Allows you to fly with elytra", category = Module.Category.MOVEMENT)
public class ElytraFlyModule extends Module {
    public ModeSetting mode = new ModeSetting("Mode", "The mode that will be used for elytra flying.", "Control", new String[]{"Packet", "Control"});
    public NumberSetting horizontal = new NumberSetting("Horizontal", "The speed at which you will be flying horizontally.", 2.0f, 0.1f, 10.0f);
    public NumberSetting vertical = new NumberSetting("Vertical", "The speed at which you will be flying vertically.", 1.0f, 0.1f, 10.0f);
    public BooleanSetting moveVertically = new BooleanSetting("Move", "Whether or not to allow for vertical movement.", true);
    public BooleanSetting infiniteDurability = new BooleanSetting("Infinite", "Prevents your elytra from having any durability used up.", false);
    public BooleanSetting stopOnGround = new BooleanSetting("StopOnGround", "Stops flying when you hit the ground.", true);
    public ModeSetting ncpStrict = new ModeSetting("Strict", "Makes use of special bypasses for the NoCheatPlus anticheat.", "None", new String[]{"None", "Old", "New", "Motion"});

    private float pitch;

    @SubscribeEvent
    public void onPlayerMove(PlayerMoveEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (!mode.getValue().equalsIgnoreCase("Packet")) return;

        mc.player.getAbilities().flying = false;
        mc.player.getAbilities().setFlySpeed(0.05F);

        if ((mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().expand(-0.25, 0.0, -0.25).offset(0.0, -0.3, 0.0)).iterator().hasNext() && stopOnGround.getValue()) 
            || mc.player.getInventory().getStack(38).getItem() != Items.ELYTRA) return;

        mc.player.getAbilities().flying = true;
        mc.player.getAbilities().setFlySpeed(horizontal.getValue().floatValue() / 15.0f);
        event.setCancelled(true);

        if (Math.abs(event.getX()) < 0.05) event.setX(0);
        if (Math.abs(event.getZ()) < 0.05) event.setZ(0);

        event.setY(moveVertically.getValue() ? 
            mc.options.jumpKey.isPressed() ? vertical.getValue().doubleValue() : 
            mc.options.sneakKey.isPressed() ? -vertical.getValue().doubleValue() : 0 : 0);

        switch (ncpStrict.getValue().toLowerCase()) {
            case "old" -> event.setY(0.0002 - (mc.player.age % 2 == 0 ? 0 : 0.000001));
            case "new" -> event.setY(-1.000088900582341E-12);
            case "motion" -> event.setY(-4.000355602329364E-12);
        }

        if (mc.player.horizontalCollision && 
            (ncpStrict.getValue().equalsIgnoreCase("New") || ncpStrict.getValue().equalsIgnoreCase("Motion")) && 
            mc.player.age % 2 == 0) {
            event.setY(-0.07840000152587923);
        }
    }

    @SubscribeEvent
    public void onSendMovement(SendMovementEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (!mode.getValue().equalsIgnoreCase("Packet")) return;

        if ((!mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().expand(-0.25, 0.0, -0.25).offset(0.0, -0.3, 0.0)).iterator().hasNext() || !stopOnGround.getValue()) 
            && mc.player.getInventory().getStack(38).getItem() == Items.ELYTRA) {
            if (infiniteDurability.getValue() || !mc.player.isGliding()) {
                mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            }
            if (mc.player.age % 3 != 0 && ncpStrict.getValue().equalsIgnoreCase("Motion")) {
                event.setCancelled(true);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTravel(PlayerTravelEvent event) {
        if (mc.player == null || mc.world == null || !mc.player.isGliding()) return;
        if (!mode.getValue().equalsIgnoreCase("Control")) return;

        event.setCancelled(true);

        if (mc.player.input.movementForward == 0.0f && mc.player.input.movementSideways == 0.0f) {
            mc.player.setVelocity(new Vec3d(0.0, mc.player.getVelocity().getY(), 0.0));
        } else {
            pitch = 12;
            double cos = Math.cos(Math.toRadians(mc.player.getYaw() + 90.0f));
            double sin = Math.sin(Math.toRadians(mc.player.getYaw() + 90.0f));
            
            mc.player.setVelocity(new Vec3d(
                (mc.player.input.movementForward * horizontal.getValue().doubleValue() * cos) + 
                (mc.player.input.movementSideways * horizontal.getValue().doubleValue() * sin),
                mc.player.getVelocity().getY(),
                (mc.player.input.movementForward * horizontal.getValue().doubleValue() * sin) - 
                (mc.player.input.movementSideways * horizontal.getValue().doubleValue() * cos)
            ));
        }

        mc.player.setVelocity(new Vec3d(mc.player.getVelocity().getX(), 0.0, mc.player.getVelocity().getZ()));

        if (moveVertically.getValue()) {
            if (mc.options.jumpKey.isPressed()) {
                mc.player.setVelocity(new Vec3d(mc.player.getVelocity().getX(), vertical.getValue().doubleValue(), mc.player.getVelocity().getZ()));
                pitch = -51;
            } else if (mc.options.sneakKey.isPressed()) {
                mc.player.setVelocity(new Vec3d(mc.player.getVelocity().getX(), -vertical.getValue().doubleValue(), mc.player.getVelocity().getZ()));
                pitch = 0;
            }
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent event) {
        if (mc.player == null || mc.world == null) return;

        if (mode.getValue().equalsIgnoreCase("Packet")) {
            if (event.getPacket() instanceof EntityTrackerUpdateS2CPacket packet && packet.id() == mc.player.getId()) {
                List<DataTracker.SerializedEntry<?>> values = packet.trackedValues();
                if (values.isEmpty()) return;

                for (DataTracker.SerializedEntry<?> value : values) {
                    if (value.value().toString().equals("FALL_FLYING") || 
                        (value.id() == 0 && (value.value().toString().equals("-120") || 
                                           value.value().toString().equals("-128") || 
                                           value.value().toString().equals("-126")))) {
                        event.setCancelled(true);
                    }
                }
            }
        }

        if (mode.getValue().equalsIgnoreCase("Control")) {
            if (event.getPacket() instanceof PlayerMoveC2SPacket packet && packet.changesLook() && mc.player.isGliding()) {
                if (mc.options.leftKey.isPressed()) {
                    ((PlayerMoveC2SPacketAccessor) packet).setYaw(packet.getYaw(0.0f) - 90.0f);
                }
                if (mc.options.rightKey.isPressed()) {
                    ((PlayerMoveC2SPacketAccessor) packet).setYaw(packet.getYaw(0.0f) + 90.0f);
                }
                ((PlayerMoveC2SPacketAccessor) packet).setPitch(pitch);
            }
        }
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        mc.player.getAbilities().flying = false;
        mc.player.getAbilities().setFlySpeed(0.05F);
    }

    @Override
    public String getMetaData() {
        return mode.getValue();
    }
}
