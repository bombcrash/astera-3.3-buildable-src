package me.lyrica.modules.impl.movement;

import me.lyrica.Lyrica;
import me.lyrica.events.SubscribeEvent;
import me.lyrica.events.impl.PlayerMoveEvent;
import me.lyrica.modules.Module;
import me.lyrica.modules.RegisterModule;
import me.lyrica.settings.impl.BooleanSetting;
import me.lyrica.settings.impl.NumberSetting;
import me.lyrica.utils.minecraft.HoleUtils;
import me.lyrica.utils.minecraft.MovementUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RegisterModule(name = "HoleAnchor", description = "Pulls you toward your nearest hole.", category = Module.Category.MOVEMENT)
public class HoleSnapModule extends Module {
    public static HoleSnapModule INSTANCE;
    
    public HoleSnapModule() {
        INSTANCE = this;
    }

    public NumberSetting range = new NumberSetting("Range", "Range for the holes.", 5, 1, 8);
    public BooleanSetting doubleHoles = new BooleanSetting("DoubleHoles", "Whether or not to snap you to double holes.", true);
    public BooleanSetting quadHoles = new BooleanSetting("Quad", "Whether or not to snap you to quad holes.", true);
    public BooleanSetting step = new BooleanSetting("AutoStep", "Automatically steps when trying to holesnap.", false);

    public Box hole = null;

    @Override
    public void onEnable() {
        hole = null;
    }

    @SubscribeEvent
    public void onPlayerMove(PlayerMoveEvent event) {
        if (getNull() || mc.player.fallDistance >= 5.0f) return;

        List<HoleUtils.Hole> holes = getHoles();
        if(holes.isEmpty()) return;

        hole = holes.get(0).box();

        if(mc.player.getX() == hole.getCenter().x && mc.player.getY() == hole.minY && mc.player.getZ() == hole.getCenter().z) {
            if(Lyrica.MODULE_MANAGER.getModule(StepModule.class).isToggled()) Lyrica.MODULE_MANAGER.getModule(StepModule.class).setToggled(false);
            if(Lyrica.MODULE_MANAGER.getModule(SpeedModule.class).isToggled()) Lyrica.MODULE_MANAGER.getModule(SpeedModule.class).setToggled(false);
            setToggled(false);
            return;
        }

        MovementUtils.moveTowards(event, hole.getCenter(), MovementUtils.getPotionSpeed(MovementUtils.DEFAULT_SPEED));
    }

    private List<HoleUtils.Hole> getHoles() {
        List<HoleUtils.Hole> holes = new ArrayList<>();

        for (int i = 0; i < Lyrica.WORLD_MANAGER.getRadius(range.getValue().doubleValue()); i++) {
            BlockPos position = mc.player.getBlockPos().add(Lyrica.WORLD_MANAGER.getOffset(i));

            if(position.getY() > mc.player.getY()) continue;

            HoleUtils.Hole singleHole = HoleUtils.getSingleHole(position, 1);
            if (singleHole != null) {
                holes.add(singleHole);
                continue;
            }

            if (doubleHoles.getValue()) {
                HoleUtils.Hole doubleHole = HoleUtils.getDoubleHole(position, 1);
                if (doubleHole != null) {
                    holes.add(doubleHole);
                    continue;
                }
            }

            if (quadHoles.getValue()) {
                HoleUtils.Hole quadHole = HoleUtils.getQuadHole(position, 1);
                if (quadHole != null) {
                    holes.add(quadHole);
                }
            }
        }

        return holes.stream().sorted(Comparator.comparing(h -> mc.player.squaredDistanceTo(h.box().getCenter().x, h.box().getCenter().y, h.box().getCenter().z))).toList();
    }
}
