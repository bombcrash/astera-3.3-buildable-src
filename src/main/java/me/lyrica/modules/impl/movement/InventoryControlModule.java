package me.lyrica.modules.impl.movement;

import me.lyrica.events.SubscribeEvent;
import me.lyrica.events.impl.TickEvent;
import me.lyrica.mixins.accessors.CreativeInventoryScreenAccessor;
import me.lyrica.modules.Module;
import me.lyrica.modules.RegisterModule;
import me.lyrica.settings.impl.BooleanSetting;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemGroup;

@RegisterModule(name = "InventoryMove", description = "Allows you to control things such as movement and your camera while a GUI is open.", category = Module.Category.MOVEMENT)
public class InventoryControlModule extends Module {
    public BooleanSetting movement = new BooleanSetting("Movement", "Allows you to control movement.", true);
    public BooleanSetting portals = new BooleanSetting("Portals", "Allows you to interact with GUIs while inside of a portal.", true);

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (mc.player == null) return;

        if (movement.getValue() && mc.currentScreen != null && !(mc.currentScreen instanceof ChatScreen || mc.currentScreen instanceof BookEditScreen || mc.currentScreen instanceof SignEditScreen || mc.currentScreen instanceof JigsawBlockScreen || mc.currentScreen instanceof StructureBlockScreen || mc.currentScreen instanceof AnvilScreen || (mc.currentScreen instanceof CreativeInventoryScreen && CreativeInventoryScreenAccessor.getSelectedTab().getType() == ItemGroup.Type.SEARCH))) {
            for (KeyBinding binding : new KeyBinding[]{mc.options.forwardKey, mc.options.backKey, mc.options.rightKey, mc.options.leftKey, mc.options.sprintKey, mc.options.sneakKey, mc.options.jumpKey}) {
                binding.setPressed(InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.fromTranslationKey(binding.getBoundKeyTranslationKey()).getCode()));
            }
        }
    }
}
