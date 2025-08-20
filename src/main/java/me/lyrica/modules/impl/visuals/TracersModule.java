package me.lyrica.modules.impl.visuals;

import me.lyrica.Lyrica;
import me.lyrica.events.SubscribeEvent;
import me.lyrica.events.impl.RenderWorldEvent;
import me.lyrica.modules.Module;
import me.lyrica.modules.RegisterModule;
import me.lyrica.settings.impl.BooleanSetting;
import me.lyrica.settings.impl.ColorSetting;
import me.lyrica.settings.impl.ModeSetting;
import me.lyrica.utils.color.ColorUtils;
import me.lyrica.utils.graphics.Renderer3D;
import me.lyrica.utils.minecraft.EntityUtils;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

@RegisterModule(name = "Tracers", description = "Renders a line showing where other players are located.", category = Module.Category.VISUALS)
public class TracersModule extends Module {
    public BooleanSetting antiBot = new BooleanSetting("AntiBot", "Prevents bots from having arrow tracers rendered for them.", false);
    public ModeSetting mode = new ModeSetting("Mode", "The mode for the tracers color.", "Distance", new String[]{"Distance", "Custom"});
    public ColorSetting color = new ColorSetting("Color", "The color used for the fill rendering.", ColorUtils.getDefaultOutlineColor());

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event) {
        if(getNull()) return;

        if (mc.player == null) {
            return;
        }
        boolean prevBobView = mc.options.getBobView().getValue();
        mc.options.getBobView().setValue(false);

        Vec3d pos = EntityUtils.getRenderPos(mc.player, event.getTickDelta());
        Camera camera = mc.gameRenderer.getCamera();
        Vec3d cameraPos = new Vec3d(0.0, 0.0, 1.0).rotateX(-(float) Math.toRadians(camera.getPitch())).rotateY(-(float) Math.toRadians(camera.getYaw())).add(pos.x, mc.player.getEyeHeight(mc.player.getPose()) + pos.y, pos.z);

        for(PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;
            if (EntityUtils.isBot(player) && antiBot.getValue()) continue;

            Vec3d playerPos = EntityUtils.getRenderPos(player, event.getTickDelta());
            Renderer3D.renderLine(event.getMatrices(), cameraPos, playerPos, getColor(player));
        }

        mc.options.getBobView().setValue(prevBobView);
    }

    private Color getColor(PlayerEntity player) {
        if(Lyrica.FRIEND_MANAGER.contains(player.getName().getString())) return Lyrica.FRIEND_MANAGER.getDefaultFriendColor(color.getColor().getAlpha());

        if(mode.getValue().equals("Custom")) return color.getColor();

        float maxDistance = 80;
        float distance = MathHelper.clamp(mc.player.distanceTo(player), 0, maxDistance);
        return new Color(((maxDistance - distance) / maxDistance), 1.0f - (maxDistance - distance) / (float) maxDistance, 0, color.getColor().getAlpha()/255f);
    }
}
