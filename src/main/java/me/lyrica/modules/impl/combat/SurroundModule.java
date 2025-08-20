package me.lyrica.modules.impl.combat;

import me.lyrica.Lyrica;
import me.lyrica.events.SubscribeEvent;
import me.lyrica.events.impl.PacketReceiveEvent;
import me.lyrica.events.impl.PlayerJumpEvent;
import me.lyrica.events.impl.PlayerUpdateEvent;
import me.lyrica.modules.Module;
import me.lyrica.modules.RegisterModule;
import me.lyrica.modules.impl.movement.HitboxDesyncModule;
import me.lyrica.modules.impl.movement.SpeedModule;
import me.lyrica.modules.impl.movement.StepModule;
import me.lyrica.settings.impl.BooleanSetting;
import me.lyrica.settings.impl.ModeSetting;
import me.lyrica.settings.impl.NumberSetting;
import me.lyrica.utils.minecraft.HoleUtils;
import me.lyrica.utils.minecraft.InventoryUtils;
import me.lyrica.utils.minecraft.PositionUtils;
import me.lyrica.utils.minecraft.WorldUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RegisterModule(name = "FeetTrap", description = "Automatically places blocks at your feet to prevent crystal damage.", category = Module.Category.COMBAT)
public class SurroundModule extends Module {
    public ModeSetting autoSwitch = new ModeSetting("Switch", "The mode that will be used for automatically switching to necessary items.", "Silent", InventoryUtils.SWITCH_MODES);
    public ModeSetting timing = new ModeSetting("Timings", "The timing that will be used in replacing broken surround blocks.", "Sequential", new String[]{"Vanilla", "Sequential"});
    public NumberSetting limit = new NumberSetting("Limit", "The maximum number of blocks that can be placed each group.", 4, 1, 20);
    public NumberSetting delay = new NumberSetting("TPS", "The delay in ticks between each group of placements.", 0, 0, 20);
    public NumberSetting range = new NumberSetting("Range", "The maximum range at which the blocks will be placed at.", 5.0, 0.0, 12.0);
    public BooleanSetting await = new BooleanSetting("Await", "Waits for blocks to be registered by the client before placing on them.", false);
    public BooleanSetting rotate = new BooleanSetting("Rotate", "Whether or not you should rotate when you place blocks.", true);
    public BooleanSetting strictDirection = new BooleanSetting("StrictDirection", "Only places using directions that face you.", false);
    public BooleanSetting crystalDestruction = new BooleanSetting("CrystalDestruction", "Destroys any crystals that interfere with block placement.", true);
    public BooleanSetting center = new BooleanSetting("Center", "Puts you in the center of the block when you surround.", false);
    public BooleanSetting floor = new BooleanSetting("Floor", "Places blocks under your feet as well.", true);
    public BooleanSetting extension = new BooleanSetting("Extension", "Extends the surround if there are entities obstructing block placement.", true);
    public BooleanSetting whileEating = new BooleanSetting("WhileEating", "Places blocks normally while eating.", true);

    public BooleanSetting selfDisable = new BooleanSetting("SelfDisable", "Toggles off the module once it is finished with placing.", false);
    public BooleanSetting jumpDisable = new BooleanSetting("JumpDisable", "Toggles off the module whenever your Y level changes.", true);
    public BooleanSetting itemDisable = new BooleanSetting("ItemDisable", "Toggles off the module whenever you run out of items to place with.", true);

    public BooleanSetting stepToggle = new BooleanSetting("AutoStep", "Toggles the step module when you surround.", false);
    public BooleanSetting speedToggle = new BooleanSetting("AutoSpeed", "Toggles the speed module when you surround.", false);

    public BooleanSetting render = new BooleanSetting("Render", "Whether or not to render the place position.", true);

    public BooleanSetting attack = new BooleanSetting("Attack", "Blok yerleştirirken varlıklara saldır.", true);
    public BooleanSetting swing = new BooleanSetting("Swing", "Saldırı sırasında el sallama animasyonu.", true);
    public BooleanSetting simulate = new BooleanSetting("Simulate", "Yerleştirilen blokları simüle et.", false);

    private Set<BlockPos> targetPositions = new HashSet<>();
    private BlockPos lastPosition = null;

    private int ticks = 0;
    private int blocksPlaced = 0;

    @Override
    public void onEnable() {
        if (mc.player == null || mc.world == null) return;
        lastPosition = PositionUtils.getFlooredPosition(mc.player);

        if(stepToggle.getValue() && Lyrica.MODULE_MANAGER.getModule(StepModule.class).isToggled()) Lyrica.MODULE_MANAGER.getModule(StepModule.class).setToggled(false);
        if(speedToggle.getValue() && Lyrica.MODULE_MANAGER.getModule(SpeedModule.class).isToggled()) Lyrica.MODULE_MANAGER.getModule(SpeedModule.class).setToggled(false);
        if(center.getValue()) mc.player.setPosition(lastPosition.getX() + 0.5, lastPosition.getY(), lastPosition.getZ() + 0.5);
    }

    @SubscribeEvent
    public void onPlayerJump(PlayerJumpEvent event) {
        if (jumpDisable.getValue()) {
            setToggled(false);
        }
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (jumpDisable.getValue() && (mc.player.fallDistance > 2.0f || ((Lyrica.MODULE_MANAGER.getModule(StepModule.class).isToggled() || Lyrica.MODULE_MANAGER.getModule(SpeedModule.class).isToggled()) && (lastPosition == null || lastPosition.getY() != PositionUtils.getFlooredPosition(mc.player).getY())))) {
            setToggled(false);
            return;
        }

        if (!whileEating.getValue() && mc.player.isUsingItem()) return;
        blocksPlaced = 0;

        if (ticks < delay.getValue().intValue()) {
            ticks++;
            return;
        }

        if (autoSwitch.getValue().equalsIgnoreCase("None") && !(mc.player.getMainHandStack().getItem() instanceof BlockItem)) {
            if (itemDisable.getValue()) {
                Lyrica.CHAT_MANAGER.tagged("You are currently not holding any blocks.", getName());
                setToggled(false);
            }

            targetPositions.clear();
            return;
        }

        int slot = InventoryUtils.findHardestBlock(0, 8);
        int previousSlot = mc.player.getInventory().selectedSlot;

        if (slot == -1) {
            if (itemDisable.getValue()) {
                Lyrica.CHAT_MANAGER.tagged("No blocks could be found in your hotbar.", getName());
                setToggled(false);
            }

            targetPositions.clear();
            return;
        }

        targetPositions = HoleUtils.getFeetPositions(mc.player, extension.getValue(), floor.getValue(), false);

        HitboxDesyncModule module = Lyrica.MODULE_MANAGER.getModule(HitboxDesyncModule.class);
        List<BlockPos> positions = targetPositions.stream().filter(position -> mc.player.squaredDistanceTo(Vec3d.ofCenter(position)) <= MathHelper.square(range.getValue().doubleValue()))
                .filter(position -> WorldUtils.isPlaceable(position, module.isToggled() && !module.close.getValue()))
                .toList();

        if (positions.isEmpty()) {
            if (selfDisable.getValue()) setToggled(false);
            return;
        }

        InventoryUtils.switchSlot(autoSwitch.getValue(), slot, previousSlot);

        List<BlockPos> placedPositions = new ArrayList<>();
        List<BlockPos> simulatedBlocks = new ArrayList<>();
        for (BlockPos position : positions) {
            if (blocksPlaced >= limit.getValue().intValue()) break;

            Direction direction = WorldUtils.getDirection(position, placedPositions, strictDirection.getValue());
            if (direction == null) {
                BlockPos supportPosition = position.add(0, -1, 0);
                if (!WorldUtils.isPlaceable(supportPosition)) continue;

                Direction supportDirection = WorldUtils.getDirection(supportPosition, placedPositions, strictDirection.getValue());
                if (supportDirection == null) continue;

                boolean placed = placeBlockWithRetry(supportPosition, supportDirection, Hand.MAIN_HAND, rotate.getValue(), attack.getValue(), render.getValue());
                if (placed) {
                placedPositions.add(supportPosition);
                blocksPlaced++;
                }

                if (blocksPlaced >= limit.getValue().intValue()) break;
                if (await.getValue()) continue;

                direction = WorldUtils.getDirection(position, placedPositions, strictDirection.getValue());
                if (direction == null) continue;
            }

            boolean placed = placeBlockWithRetry(position, direction, Hand.MAIN_HAND, rotate.getValue(), attack.getValue(), render.getValue());
            if (placed) {
            placedPositions.add(position);
            blocksPlaced++;
                if (simulate.getValue()) {
                    simulatedBlocks.add(position);
                }
            }
        }

        InventoryUtils.switchBack(autoSwitch.getValue(), slot, previousSlot);

        ticks = 0;
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (!timing.getValue().equalsIgnoreCase("Sequential"))
            return;

        if (event.getPacket() instanceof EntitySpawnS2CPacket packet && packet.getEntityType().equals(EntityType.END_CRYSTAL)) {
            EndCrystalEntity crystal = new EndCrystalEntity(mc.world, packet.getX(), packet.getY(), packet.getZ());

            for (BlockPos position : targetPositions) {
                if (new Box(position).intersects(crystal.getBoundingBox()) && targetPositions.contains(position)) {

                    if (blocksPlaced > limit.getValue().intValue()) return;
                    if (!whileEating.getValue() && mc.player.isUsingItem()) return;

                    int slot = InventoryUtils.findHardestBlock(0, autoSwitch.getValue().equalsIgnoreCase("AltSwap") || autoSwitch.getValue().equalsIgnoreCase("AltPickup") ? 35 : 8);
                    int previousSlot = mc.player.getInventory().selectedSlot;

                    if (slot == -1) return;

                    Direction direction = WorldUtils.getDirection(position, strictDirection.getValue());
                    if (direction == null) return;

                    InventoryUtils.switchSlot(autoSwitch.getValue(), slot, previousSlot);

                    boolean placed = placeBlockWithRetry(position, direction, Hand.MAIN_HAND, rotate.getValue(), attack.getValue(), render.getValue());
                    if (placed) {
                    blocksPlaced++;
                        if (simulate.getValue()) {
                            // Simülasyon için ekleme
                        }
                    }

                    InventoryUtils.switchBack(autoSwitch.getValue(), slot, previousSlot);
                    break;
                }
            }
        }
    }

    @SubscribeEvent
    public void onDisable() {
        lastPosition = null;
        targetPositions.clear();

        ticks = 0;
        blocksPlaced = 0;
    }

    @Override
    public String getMetaData() {
        return String.valueOf(targetPositions.size());
    }

    private boolean placeBlockWithRetry(BlockPos position, Direction direction, Hand hand, boolean rotate, boolean attackEntities, boolean renderBlock) {
        try {
            if (attackEntities) {
                List<Entity> entities = mc.world.getOtherEntities(null, new Box(position)).stream()
                        .filter(entity -> !(entity instanceof EndCrystalEntity) && 
                                  !(entity instanceof net.minecraft.entity.ExperienceOrbEntity) && 
                                  !(entity instanceof net.minecraft.entity.ItemEntity) &&
                                  !(entity.equals(mc.player)))
                        .toList();

                if (!entities.isEmpty()) {
                    // Varlıklara saldır ve blok yerleştirmeyi ertele
                    for (Entity entity : entities) {
                        mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(entity, mc.player.isSneaking()));
                        if (swing.getValue()) {
                            mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                        }
                        break;
                    }
                    return false;
                }
            }

            WorldUtils.destroyCrystals(position);
            WorldUtils.placeBlock(position, direction, hand, rotate, true, renderBlock);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
