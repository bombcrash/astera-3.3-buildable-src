package me.lyrica.modules.impl.player;

import me.lyrica.modules.Module;
import me.lyrica.modules.RegisterModule;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;

@RegisterModule(name = "MultiTask", description = "Allows you to interact with blocks while eating or using an item.", category = Module.Category.PLAYER)
public class MultiTaskModule extends Module {
    public void onUpdate() {
        if (mc.crosshairTarget instanceof BlockHitResult crossHair && crossHair.getBlockPos() != null && mc.options.attackKey.isPressed() && !mc.world.getBlockState(crossHair.getBlockPos()).isAir()) {
            mc.interactionManager.attackBlock(crossHair.getBlockPos(), crossHair.getSide());
            mc.player.swingHand(Hand.MAIN_HAND);
        }

        if (mc.crosshairTarget instanceof EntityHitResult ehr && ehr.getEntity() != null && mc.options.attackKey.isPressed() && mc.player.getAttackCooldownProgress(0.5f) > 0.9f) {
            mc.interactionManager.attackEntity(mc.player, ehr.getEntity());
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }
}
