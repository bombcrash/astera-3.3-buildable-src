package me.lyrica.modules.impl.movement;

import me.lyrica.events.SubscribeEvent;
import me.lyrica.events.impl.RenderWorldEvent;
import me.lyrica.mixins.accessors.CreativeInventoryScreenAccessor;
import me.lyrica.modules.Module;
import me.lyrica.modules.RegisterModule;
import me.lyrica.settings.impl.BooleanSetting;
import me.lyrica.settings.impl.NumberSetting;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

public class ArrowControlsModule extends Module {
    public NumberSetting speed = new NumberSetting("Speed", "The speed at which the camera will be moving at.", 1.0f, 0.1f, 10.0f);
    public BooleanSetting inventory = new BooleanSetting("Inventory", "Allows you to control the camera when a screen is opened.", true);

    private long lastTime;

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (mc.currentScreen != null && (!inventory.getValue() || (mc.currentScreen instanceof ChatScreen || mc.currentScreen instanceof BookEditScreen || mc.currentScreen instanceof SignEditScreen || mc.currentScreen instanceof JigsawBlockScreen || mc.currentScreen instanceof StructureBlockScreen || mc.currentScreen instanceof AnvilScreen || (mc.currentScreen instanceof CreativeInventoryScreen && CreativeInventoryScreenAccessor.getSelectedTab().getType() == ItemGroup.Type.SEARCH))))
            return;

        float yaw = 0.0f;
        float pitch = 0.0f;

        float amount = ((System.currentTimeMillis() - lastTime) / 10f) * speed.getValue().floatValue();
        lastTime = System.currentTimeMillis();

        if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT)) yaw -= amount;
        if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT)) yaw += amount;
        if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_UP)) pitch -= amount;
        if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_DOWN)) pitch += amount;

        mc.player.setYaw(mc.player.getYaw() + yaw);
        mc.player.setPitch(MathHelper.clamp(mc.player.getPitch() + pitch, -90.0f, 90.0f));
    }

    @Override
    public String getMetaData() {
        return String.valueOf(speed.getValue().floatValue());
    }
}
