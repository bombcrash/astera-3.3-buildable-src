package me.lyrica.modules.impl.combat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.lyrica.Lyrica;
import me.lyrica.events.SubscribeEvent;
import me.lyrica.events.impl.*;
import me.lyrica.modules.Module;
import me.lyrica.modules.RegisterModule;
import me.lyrica.modules.impl.player.SpeedMineModule;
import me.lyrica.modules.impl.player.ThrowXPModule;
import me.lyrica.settings.impl.*;
import me.lyrica.utils.color.ColorUtils;
import me.lyrica.utils.minecraft.DamageUtils;
import me.lyrica.utils.minecraft.InventoryUtils;
import me.lyrica.utils.minecraft.PositionUtils;
import me.lyrica.utils.minecraft.WorldUtils;
import me.lyrica.utils.rotations.RotationUtils;
import me.lyrica.utils.system.Counter;
import me.lyrica.utils.system.Timer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ExperienceBottleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.ExperienceOrbSpawnS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

@RegisterModule(name = "CrystalAura", description = "Automatically places and attacks crystals to annihilate your opponents.", category = Module.Category.COMBAT)
public class AutoCrystalModule extends Module {
    public CategorySetting placeCategory = new CategorySetting("Place", "The category for settings related to placing crystals.");
    public BooleanSetting place = new BooleanSetting("Placement", "Enabled", "Automatically places crystals on positions that are deemed safe and lethal enough.", new CategorySetting.Visibility(placeCategory), true);
    public NumberSetting placeSpeed = new NumberSetting("PlaceTPS", "TPS", "The speed at which crystals will be placed.", new CategorySetting.Visibility(placeCategory), 20.0f, 0.1f, 20.0f);
    public NumberSetting placeRange = new NumberSetting("Range", "Range", "The maximum distance at which positions will be placed on.", new CategorySetting.Visibility(placeCategory), 4.5, 0.0, 8.0);
    public NumberSetting placeWallsRange = new NumberSetting("WallsRange", "WallsRange", "The maximum distance at which positions will be placed on through walls.", new CategorySetting.Visibility(placeCategory), 4.5, 0.0, 8.0);
    public ModeSetting placements = new ModeSetting("PlaceMode", "The version of the game that will be used for crystal placement calculations.", new CategorySetting.Visibility(placeCategory), "NewProtocol", new String[]{"NewProtocol", "OldProtocol"});
    public BooleanSetting blockDestruction = new BooleanSetting("BlockDestruction", "Places crystals on top of mined blocks in order to damage opponents.", new CategorySetting.Visibility(placeCategory), true);
    public ModeSetting autoSwitch = new ModeSetting("SwapMode", "Automatically switches to a crystal if you aren't currently holding one.", new CategorySetting.Visibility(placeCategory), "None", new String[]{"None", "Normal", "Silent", "Alternative"});
    public BooleanSetting attack = new BooleanSetting("Attack", "Enabled", "Automatically attacks crystals that are deemed safe.", new CategorySetting.Visibility(placeCategory), true);
    public NumberSetting attackSpeed = new NumberSetting("AttackSpeed", "Speed", "The speed at which crystals will be attacked.", new CategorySetting.Visibility(placeCategory), 20.0f, 0.1f, 20.0f);
    public NumberSetting attackRange = new NumberSetting("AttackRange", "Range", "The maximum distance at which crystals will be attacked.", new CategorySetting.Visibility(placeCategory), 4.5, 0.0, 8.0);
    public NumberSetting attackWallsRange = new NumberSetting("AttackWallsRange", "WallsRange", "The maximum distance at which crystals will be attacked through walls.", new CategorySetting.Visibility(placeCategory), 4.5, 0.0, 8.0);
    public ModeSetting antiWeakness = new ModeSetting("AntiWeakness", "Allows you to attack crystals when weaknessed.", new CategorySetting.Visibility(placeCategory), "None", new String[]{"None", "Normal", "Silent"});
    public BooleanSetting instant = new BooleanSetting("Instant", "Instantly attacks crystals once they spawn.", new CategorySetting.Visibility(placeCategory), true);
    public BooleanSetting inhibit = new BooleanSetting("Inhibit", "Prevents excessive attacks on crystals by blacklisting crystals when attacking them.", new CategorySetting.Visibility(placeCategory), true);
    public BooleanSetting godSync = new BooleanSetting("IDSpoof", "Makes the attacking way faster by predicting entity IDs.", new CategorySetting.Visibility(placeCategory), false);
    public NumberSetting predictions = new NumberSetting("Predictions", "The amount of predictions that will be done after placing.", new CategorySetting.Visibility(placeCategory), 10, 1, 20);
    public NumberSetting offset = new NumberSetting("Offset", "The amount that the last entity ID should be offset by.", new CategorySetting.Visibility(placeCategory), 0, 0, 2);
    public ModeSetting godSwing = new ModeSetting("PredictSwing", "Swing", "The swinging that will be done for each predicted attack.", new CategorySetting.Visibility(placeCategory), "Normal", new String[]{"None", "Normal", "Strict"});
    public BooleanSetting fast = new BooleanSetting("Fast", "Improves the speed of the prediction calculations at the cost of stability.", new CategorySetting.Visibility(placeCategory), false);
    public BooleanSetting antiKick = new BooleanSetting("InvalidSafe", "Prevents you from getting kicked by attacking invalid entity IDs.", new CategorySetting.Visibility(placeCategory), false);
    public NumberSetting kickThreshold = new NumberSetting("KickThreshold", "Threshold", "The tick threshold for the kick prevention.", new BooleanSetting.Visibility(antiKick, true), 5, 1, 10);

    public CategorySetting miscellaneousCategory = new CategorySetting("Misc", "The category for all miscellaneous settings.");
    public ModeSetting sequential = new ModeSetting("executionOrder", "The sequence that the module's processes will be run in.", new CategorySetting.Visibility(miscellaneousCategory), "Strong", new String[]{"None", "Strict", "Strong"});
    public ModeSetting rotate = new ModeSetting("RotationMode", "Automatically rotates to the crystal whenever attacking or placing.", new CategorySetting.Visibility(miscellaneousCategory), "Normal", new String[]{"None", "Normal", "Packet"});
    public ModeSetting swing = new ModeSetting("SwingMode", "The hand that will be used for swinging.", new CategorySetting.Visibility(miscellaneousCategory), "Default", new String[]{"Default", "None", "Packet", "Mainhand", "Offhand", "Both"});
    public BooleanSetting yawStep = new BooleanSetting("YawStep", "Performs your rotations over multiple ticks.", new CategorySetting.Visibility(miscellaneousCategory), false);
    public NumberSetting yawStepThreshold = new NumberSetting("Threshold", "Threshold", "The threshold in order for yaw to be modified.", new BooleanSetting.Visibility(yawStep, true), 75, 1, 180);
    public BooleanSetting raytrace = new BooleanSetting("Raytrace", "Avoids attacking or placing any crystals through walls.", new CategorySetting.Visibility(miscellaneousCategory), false);
    public NumberSetting extrapolation = new NumberSetting("Extrapolation", "Extrapolates the target's position to calculate positions ahead of time.", new CategorySetting.Visibility(miscellaneousCategory), 0, 0, 20);
    public NumberSetting enemyRange = new NumberSetting("EnemyRange", "The maximum distance at which enemies can be at.", new CategorySetting.Visibility(miscellaneousCategory), 10.0, 0.0, 24.0);
    public BooleanSetting chestBreak = new BooleanSetting("preventObbySteal", "Prevents other players from getting obsidian from ender chests by destroying the dropped items.", new CategorySetting.Visibility(miscellaneousCategory), false);
    public BooleanSetting asynchronous = new BooleanSetting("multiThreadedCalc", "Performs calculations on separate threads.", new CategorySetting.Visibility(miscellaneousCategory), true);
    public BooleanSetting gameLoop = new BooleanSetting("speedbooster", "Runs the module on loop instead of ticks.", new CategorySetting.Visibility(miscellaneousCategory), false);
    public NumberSetting loopDelay = new NumberSetting("speedboosterTPS", "The delay that has to be waited out before running the module again.", new BooleanSetting.Visibility(gameLoop, true), 50, 0, 1000);
    public ModeSetting whileEating = new ModeSetting("StopOnEating", "Places and attacks crystal while eating or using items.", new CategorySetting.Visibility(miscellaneousCategory), "Both", new String[]{"None", "Attack", "Place", "Both"});

    public CategorySetting facePlaceCategory = new CategorySetting("FaseSpam", "The category for settings relating to faceplacing.");
    public ModeSetting facePlaceMode = new ModeSetting("FSMode", "Mode", "The checks that will be done in order to faceplace.", new CategorySetting.Visibility(facePlaceCategory), "Dynamic", new String[]{"None", "Dynamic", "Always"});
    public ModeSetting facePlaceSpeed = new ModeSetting("FS-Speed", "Speed", "The speed that players will be faceplaced at.", new CategorySetting.Visibility(facePlaceCategory), "Normal", new String[]{"Normal", "Custom"});
    public NumberSetting facePlaceDelay = new NumberSetting("FSTPS", "Delay", "The ticks that have to be waited for before faceplacing again.", new ModeSetting.Visibility(facePlaceSpeed, "Custom"), 11, 0, 20);
    public BooleanSetting healthPlace = new BooleanSetting("HealthPlace", "Whether or not to faceplace when the target's health is low.", new ModeSetting.Visibility(facePlaceMode, "Dynamic"), true);
    public NumberSetting health = new NumberSetting("Health", "The health that the target needs to be at in order for the module to start faceplacing.", new BooleanSetting.Visibility(healthPlace, true), 8.0f, 0.0f, 36.0f);
    public BooleanSetting armorPlace = new BooleanSetting("ArmorFucker", "Whether or not to faceplace when the target's armor is low on durability.", new ModeSetting.Visibility(facePlaceMode, "Dynamic"), true);
    public NumberSetting percentage = new NumberSetting("Percentage", "The percentage that one of the target's armor pieces need to be at in order to start faceplacing.", new BooleanSetting.Visibility(armorPlace, true), 10, 1, 100);

    public CategorySetting damageCategory = new CategorySetting("DMG", "The category for settings related to damage calculations.");
    public NumberSetting minimumDamage = new NumberSetting("MinDMG", "Minimum", "The minimum damage that has to be dealt to enemies.", new CategorySetting.Visibility(damageCategory), 6.0, 0.0, 36.0);
    public NumberSetting maximumSelfDamage = new NumberSetting("MaxSelfDMG", "MaximumSelf", "The maximum damage that can be dealt to you by crystals.", new CategorySetting.Visibility(damageCategory), 10.0, 0.0, 36.0);
    public NumberSetting lethalMultiplier = new NumberSetting("LethalMultiplier", "The amount of crystals that the target has to be killed by in order to ignore minimum damage.", new CategorySetting.Visibility(damageCategory), 1.5f, 0.0f, 4.0f);
    public BooleanSetting antiSuicide = new BooleanSetting("AntiSelfKill", "Prevents crystals from accidentally killing you when you're low on health.", new CategorySetting.Visibility(damageCategory), true);
    public BooleanSetting ignoreTerrain = new BooleanSetting("IgnoreTerrain", "Ignores terrain that can be destroyed when calculating damage.", new CategorySetting.Visibility(damageCategory), true);

    public CategorySetting renderCategory = new CategorySetting("Visual", "Contains all of the settings relating to position rendering.");
    public ModeSetting animationMode = new ModeSetting("Animation", "The animation that will be applied to the rendering.", new CategorySetting.Visibility(renderCategory), "Static", new String[]{"Static", "Slide"});
    public ModeSetting mode = new ModeSetting("Mode", "The mode for the auto crystal render.", new CategorySetting.Visibility(renderCategory), "Fade", new String[]{"Fade", "Shrink"});
    public NumberSetting duration = new NumberSetting("Duration", "The duration for the place render.", new CategorySetting.Visibility(renderCategory), 300, 0, 1000);
    public NumberSetting slideSmoothness = new NumberSetting("Smooth", "The smoothness for the slide while place position is changing.", new CategorySetting.Visibility(renderCategory), 1, 0, 20);
    public ModeSetting renderMode = new ModeSetting("VisualMode", "The rendering that will be applied to the target position.", new CategorySetting.Visibility(renderCategory), "Both", new String[]{"None", "Fill", "Outline", "Both"});
    public ColorSetting fillColorUp = new ColorSetting("BoxColorUp", "The color that will be used for the fill gradiant upper part rendering.", new ModeSetting.Visibility(renderMode, "Fill", "Both"), ColorUtils.getDefaultFillColor());
    public ColorSetting fillColorDown = new ColorSetting("BoxColorDown", "The color that will be used for the fill gradiant lower part rendering.", new ModeSetting.Visibility(renderMode, "Fill", "Both"), ColorUtils.getDefaultFillColor());
    public ColorSetting outlineColorUp = new ColorSetting("BoxOutlineColor1", "The color that will be used for the outline gradiant upper part rendering.", new ModeSetting.Visibility(renderMode, "Outline", "Both"), ColorUtils.getDefaultOutlineColor());
    public ColorSetting outlineColorDown = new ColorSetting("BoxOutlineColor2", "The color that will be used for the outline gradiant lower part rendering.", new ModeSetting.Visibility(renderMode, "Outline", "Both"), ColorUtils.getDefaultOutlineColor());
    public BooleanSetting renderDamage = new BooleanSetting("RenderDamage", "Damage", "Renders the damage that the position will do to the opponent.", new CategorySetting.Visibility(renderCategory), false);
    public BooleanSetting icon = new BooleanSetting("Indicator", "Renders a customizable crystal icon on the rendered position.", new CategorySetting.Visibility(renderCategory), false);
    public NumberSetting iconScale = new NumberSetting("IndicatorSize", "The scaling that will be applied to the crystal icon rendering.", new BooleanSetting.Visibility(icon, true), 3, 1, 5);
    public NumberSetting iconRadius = new NumberSetting("IndicatorRadius", "The difference between the outer circle and the inner circle.", new BooleanSetting.Visibility(icon, true), 2.0f, 0.0f, 5.0f);
    public ColorSetting iconColor = new ColorSetting("IndicatorColor", "The color that will be used for the crystal icon rendering.", new BooleanSetting.Visibility(icon, true), ColorUtils.getDefaultColor());

    private static final int THREAD_COUNT = Math.max(2, Runtime.getRuntime().availableProcessors() - 1);
    private final ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

    // Flag used to avoid piling up calculation tasks in the single-thread executor.
    private final AtomicBoolean calculationRunning = new AtomicBoolean(false);

    private Runnable attackRunnable = null;
    private Runnable placeRunnable = null;

    private final Map<Integer, Long> attackedCrystals = new ConcurrentHashMap<>();
    private final Map<BlockPos, Long> placedCrystals = new ConcurrentHashMap<>();
    private final Map<BlockPos, Long> countedCrystals = new ConcurrentHashMap<>();

    private final Timer attackTimer = new Timer();
    private final Timer placeTimer = new Timer();
    private final Timer facePlaceTimer = new Timer();
    private final Timer loopTimer = new Timer();

    private boolean sequenceAttack = false;
    private boolean sequencePlace = true;

    private boolean attackedSequentially = false;
    private boolean placedSequentially = false;

    @Getter private PlayerEntity target = null;

    private EndCrystalEntity attackTarget = null;
    private PlaceTarget placeTarget = null;
    private PlaceTarget mineTarget = null;

    private String calculationTime = "0.00ms";
    private int calculationCount = 0;
    @Getter private String calculationDamage = "0.00";

    private final Counter crystalCounter = new Counter();
    private int crystalsPerSecond = 0;

    private int highestID = -100000;
    private int kickTicks = 0;

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (mc.player == null || mc.world == null) return;

        attackedCrystals.entrySet().removeIf(entry -> System.currentTimeMillis() - entry.getValue() > Lyrica.SERVER_MANAGER.getPing() * 2L);
        placedCrystals.entrySet().removeIf(entry -> System.currentTimeMillis() - entry.getValue() > Lyrica.SERVER_MANAGER.getPing() * 2L + (20 - attackSpeed.getValue().floatValue()) * 50L);
        countedCrystals.entrySet().removeIf(entry -> System.currentTimeMillis() - entry.getValue() > Lyrica.SERVER_MANAGER.getPing() * 2L);

        crystalsPerSecond = crystalCounter.getCount();

        Runnable runnable = () -> {
            long startTime = System.nanoTime();

            attackTarget = THREAD_COUNT > 1 ? calculateCrystalsParallel() : calculateCrystals();
            placeTarget = THREAD_COUNT > 1 ? calculatePlacementsParallel(null) : calculatePlacements(null);

            calculationTime = new DecimalFormat("0.00").format((System.nanoTime() - startTime) / 1000000.0) + "ms";
            calculationCount = placeTarget == null ? 0 : placeTarget.getCalculations();
            calculationDamage = placeTarget == null ? "0.00" : new DecimalFormat("0.00").format(placeTarget.getDamage());

            target = placeTarget == null ? null : placeTarget.getPlayer();

            if (blockDestruction.getValue() && asynchronous.getValue()) {
                SpeedMineModule module = Lyrica.MODULE_MANAGER.getModule(SpeedMineModule.class);
                BlockPos position = null;

                if (module.getPrimary() != null && module.getPrimary().isMining()) position = module.getPrimary().getPosition();
                if (position != null) mineTarget = THREAD_COUNT > 1 ? calculatePlacementsParallel(position) : calculatePlacements(position);
            }
        };

        if (asynchronous.getValue()) {
            // Allow only one calculation task at a time to avoid queue congestion and random stalls.
            if (calculationRunning.compareAndSet(false, true)) {
                executor.submit(() -> {
                    try {
                        runnable.run();
                    } finally {
                        calculationRunning.set(false);
                    }
                });
            }
        } else {
            runnable.run();
        }

        if (gameLoop.getValue()) return;

        run();
    }

    @SubscribeEvent
    public void onGameLoop(GameLoopEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (!gameLoop.getValue()) return;
        if (!loopTimer.hasTimeElapsed(loopDelay.getValue().longValue()))
            return;

        loopTimer.reset();
        run();

        if (attackRunnable != null) {
            attackRunnable.run();
            attackRunnable = null;
        }

        if (placeRunnable != null) {
            placeRunnable.run();
            placeRunnable = null;
        }
    }

    private void run() {
        attackRunnable = null;
        placeRunnable = null;

        if (sequential.getValue().equalsIgnoreCase("None")) {
            if (sequenceAttack) {
                sequenceAttack = false;
                sequencePlace = true;

                attackCrystals();
                return;
            }

            if (sequencePlace) {
                sequenceAttack = true;
                sequencePlace = false;

                placeCrystals(false);
            }
        } else {
            if (attack.getValue()) attackCrystals();
            if (place.getValue()) placeCrystals(false);
        }
    }

    @SubscribeEvent
    public void onUpdateMovement$POST(UpdateMovementEvent.Post event) {
        if (mc.player == null || mc.world == null) return;

        if (attackRunnable != null) attackRunnable.run();
        if (placeRunnable != null) placeRunnable.run();
    }

    @SubscribeEvent
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (mc.player == null || mc.world == null) return;

        if (!attack.getValue() || !instant.getValue()) return;
        if (!attackTimer.hasTimeElapsed(1000.0f - attackSpeed.getValue().floatValue() * 50.0f))
            return;

        if (!(event.getEntity() instanceof EndCrystalEntity crystal)) return;

        if (inhibit.getValue() && attackedCrystals.containsKey(crystal.getId())) return;
        if (!placedCrystals.containsKey(crystal.getBlockPos().down())) return;
        if (crystal.getBoundingBox().squaredMagnitude(mc.player.getEyePos()) > MathHelper.square(attackRange.getValue().doubleValue())) return;
        if (!mc.world.getWorldBorder().contains(crystal.getBlockPos())) return;
        if (!WorldUtils.canSee(crystal) && (raytrace.getValue() || crystal.getBoundingBox().squaredMagnitude(mc.player.getEyePos()) > MathHelper.square(attackWallsRange.getValue().doubleValue())))
            return;

        if (rotate.getValue().equalsIgnoreCase("Packet")) Lyrica.ROTATION_MANAGER.packetRotate(RotationUtils.getRotations(Vec3d.ofCenter(crystal.getBlockPos(), 0)));
        if (rotate.getValue().equalsIgnoreCase("Normal")) Lyrica.ROTATION_MANAGER.rotate(calculateRotations(Vec3d.ofCenter(crystal.getBlockPos(), 0)), Lyrica.ROTATION_MANAGER.getModulePriority(this));

        attack(crystal);

        attackedSequentially = true;
        if (sequential.getValue().equalsIgnoreCase("Strong")) {
            placeCrystals(true);
        }
    }

    @SubscribeEvent
    public void onDestroyBlock(DestroyBlockEvent event) {
        if (mc.player == null || mc.world == null) return;

        kickTicks = 0;

        if (!blockDestruction.getValue()) return;
        if (!placeTimer.hasTimeElapsed(1000.0f - placeSpeed.getValue().floatValue() * 50.0f)) return;

        BlockPos minedPosition = event.getPosition();
        if (minedPosition == null) return;

        int slot = InventoryUtils.findHotbar(Items.END_CRYSTAL);
        int previousSlot = mc.player.getInventory().selectedSlot;
        boolean switched = false;

        if (!autoSwitch.getValue().equalsIgnoreCase("None") && slot == -1 && (mc.player.getMainHandStack().getItem() != Items.END_CRYSTAL && mc.player.getOffHandStack().getItem() != Items.END_CRYSTAL))
            return;

        PlaceTarget mineTarget = this.mineTarget == null ? null : this.mineTarget.clone();
        if (mineTarget == null || (mineTarget.getPosition() != null && !minedPosition.equals(mineTarget.getException()))) mineTarget = calculatePlacements(minedPosition);
        if (mineTarget == null || mineTarget.getPosition() == null) {
            Lyrica.RENDER_MANAGER.setRenderPosition(null);
            return;
        }

        BlockPos position = mineTarget.getPosition();
        Lyrica.RENDER_MANAGER.setRenderPosition(position);

        if (mc.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter(position)) > MathHelper.square(placeRange.getValue().doubleValue())) return;
        if (!WorldUtils.canSee(position) && (raytrace.getValue() || mc.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter(position)) > MathHelper.square(placeWallsRange.getValue().doubleValue())))
            return;

        if (rotate.getValue().equalsIgnoreCase("Normal")) Lyrica.ROTATION_MANAGER.rotate(calculateRotations(Vec3d.ofCenter(position, 1)), this, Lyrica.ROTATION_MANAGER.getModulePriority(this) + 1);
        if (!rotate.getValue().equalsIgnoreCase("None")) Lyrica.ROTATION_MANAGER.packetRotate(RotationUtils.getRotations(Vec3d.ofCenter(position, 1)));

        for (Entity entity : mc.world.getOtherEntities(null, new Box(position.up())).stream().filter(entity -> entity instanceof EndCrystalEntity).toList()) {
            if (!rotate.getValue().equalsIgnoreCase("None")) Lyrica.ROTATION_MANAGER.packetRotate(RotationUtils.getRotations(entity));

            mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(entity, mc.player.isSneaking()));
            mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));

            break;
        }

        SpeedMineModule module = Lyrica.MODULE_MANAGER.getModule(SpeedMineModule.class);
        boolean flag = module.switchReset.getValue() && (module.switchMode.getValue().equalsIgnoreCase("Normal") || module.switchMode.getValue().equalsIgnoreCase("AltSwap") || module.switchMode.getValue().equalsIgnoreCase("AltPickup"));

        if (!autoSwitch.getValue().equalsIgnoreCase("None") &&  mc.player.getMainHandStack().getItem() != Items.END_CRYSTAL && mc.player.getOffHandStack().getItem() != Items.END_CRYSTAL) {
            InventoryUtils.switchSlot(flag ? "AltSwap" : autoSwitch.getValue(), slot, previousSlot);
            switched = true;
        }

        place(position);

        if (switched) {
            InventoryUtils.switchBack(flag ? "AltSwap" : autoSwitch.getValue(), slot, previousSlot);
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent event) {
        if (mc.player == null || mc.world == null) return;

        if (event.getPacket() instanceof EntitySpawnS2CPacket packet) {
            if (packet.getEntityId() > highestID) highestID = packet.getEntityId();

            BlockPos position = BlockPos.ofFloored(packet.getX(), packet.getY(), packet.getZ()).add(0, -1, 0);
            if (countedCrystals.containsKey(position)) {
                if (facePlaceTimer.hasTimeElapsed(facePlaceDelay.getValue().longValue() * 50L)) facePlaceTimer.reset();

                countedCrystals.remove(position);

                crystalCounter.increment();
                crystalsPerSecond = crystalCounter.getCount();
            }
        }

        if (event.getPacket() instanceof ExperienceOrbSpawnS2CPacket packet) {
            if (packet.getEntityId() > highestID) {
                highestID = packet.getEntityId();
            }
        }
    }

    @SubscribeEvent
    public void onPlayerDeath(PlayerDeathEvent event) {
        kickTicks = 0;
    }

    @SubscribeEvent
    public void onClientConnect(ClientConnectEvent event) {
        highestID = -100000;
    }

    @Override
    public void onDisable() {
        attackRunnable = null;
        placeRunnable = null;

        Lyrica.RENDER_MANAGER.setRenderPosition(null);

        attackedCrystals.clear();
        placedCrystals.clear();
        countedCrystals.clear();

        attackedSequentially = false;
        placedSequentially = false;

        target = null;
        placeTarget = null;
        mineTarget = null;

        calculationTime = "0.00ms";
        calculationCount = 0;
        calculationDamage = "0.00";

        crystalCounter.reset();

        highestID = -100000;
    }

    @Override
    public String getMetaData() {
        return calculationTime + ", " + calculationCount + ", " + calculationDamage + ", " + crystalsPerSecond;
    }

    private void attackCrystals() {
        EndCrystalEntity overrideCrystal = null;

        boolean flag = placeTarget != null && placeTarget.obstructions != null && !placeTarget.obstructions.isEmpty();
        for (Entity entity : flag ? placeTarget.obstructions : mc.world.getEntities()) {
            if (!(entity instanceof EndCrystalEntity crystal)) continue;
            if (!crystal.isAlive()) continue;
            if (inhibit.getValue() && attackedCrystals.containsKey(entity.getId())) continue;
            if (!flag && !placedCrystals.containsKey(crystal.getBlockPos().down())) continue;
            if (crystal.getBoundingBox().squaredMagnitude(mc.player.getEyePos()) > MathHelper.square(attackRange.getValue().doubleValue())) continue;
            if (!mc.world.getWorldBorder().contains(crystal.getBlockPos())) continue;
            if (!WorldUtils.canSee(crystal) && (raytrace.getValue() || crystal.getBoundingBox().squaredMagnitude(mc.player.getEyePos()) > MathHelper.square(attackWallsRange.getValue().doubleValue())))
                continue;

            overrideCrystal = crystal;
            break;
        }

        EndCrystalEntity crystal = overrideCrystal == null ? attackTarget : overrideCrystal;
        if (crystal == null) return;

        if (rotate.getValue().equalsIgnoreCase("Normal")) Lyrica.ROTATION_MANAGER.rotate(calculateRotations(Vec3d.ofCenter(crystal.getBlockPos(), 0)), Lyrica.ROTATION_MANAGER.getModulePriority(this));

        if (!attackTimer.hasTimeElapsed(1000.0f - attackSpeed.getValue().floatValue() * 50.0f) || attackedSequentially) {
            if (attackedSequentially) attackedSequentially = false;
            return;
        }

        Entity entity = mc.world.getEntityById(crystal.getId());
        if (entity == null) return;

        if (!(entity instanceof EndCrystalEntity endCrystal)) return;
        if (!endCrystal.isAlive()) return;
        if (inhibit.getValue() && attackedCrystals.containsKey(entity.getId())) return;
        if (endCrystal.getBoundingBox().squaredMagnitude(mc.player.getEyePos()) > MathHelper.square(attackRange.getValue().doubleValue())) return;
        if (!mc.world.getWorldBorder().contains(endCrystal.getBlockPos())) return;
        if (!WorldUtils.canSee(endCrystal) && (raytrace.getValue() || endCrystal.getBoundingBox().squaredMagnitude(mc.player.getEyePos()) > MathHelper.square(attackWallsRange.getValue().doubleValue())))
            return;

        attackRunnable = () -> {
            if (rotate.getValue().equalsIgnoreCase("Packet")) Lyrica.ROTATION_MANAGER.packetRotate(RotationUtils.getRotations(Vec3d.ofCenter(crystal.getBlockPos(), 0)));

            attack(crystal);
        };
    }

    private void placeCrystals(boolean sequential) {
        PlaceTarget placeTarget = this.placeTarget == null ? null : this.placeTarget.clone();
        if (placeTarget == null || placeTarget.getPosition() == null) {
            Lyrica.RENDER_MANAGER.setRenderPosition(null);
            return;
        }

        int slot = InventoryUtils.findHotbar(Items.END_CRYSTAL);
        int previousSlot = mc.player.getInventory().selectedSlot;

        if (!autoSwitch.getValue().equalsIgnoreCase("None") && slot == -1 && (mc.player.getMainHandStack().getItem() != Items.END_CRYSTAL && mc.player.getOffHandStack().getItem() != Items.END_CRYSTAL))
            return;

        BlockPos position = placeTarget.getPosition();
        Lyrica.RENDER_MANAGER.setRenderPosition(position);

        if (mc.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter(position)) > MathHelper.square(placeRange.getValue().doubleValue())) return;
        if (!mc.world.getWorldBorder().contains(position)) return;
        if (mc.world.getBlockState(position).getBlock() != Blocks.OBSIDIAN && mc.world.getBlockState(position).getBlock() != Blocks.BEDROCK) return;
        if (!mc.world.getBlockState(position.add(0, 1, 0)).isAir() || (placements.getValue().equalsIgnoreCase("Protocol") && !mc.world.getBlockState(position.add(0, 2, 0)).isAir())) return;
        if (!WorldUtils.canSee(position) && (raytrace.getValue() || mc.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter(position)) > MathHelper.square(placeWallsRange.getValue().doubleValue()))) return;
        if (mc.world.getOtherEntities(null, new Box(position.add(0, 1, 0))).stream().anyMatch(entity -> entity.isAlive() && !(entity instanceof ExperienceOrbEntity) && !(entity instanceof EndCrystalEntity))) return;

        if (rotate.getValue().equalsIgnoreCase("Normal")) Lyrica.ROTATION_MANAGER.rotate(calculateRotations(Vec3d.ofCenter(position, 1)), Lyrica.ROTATION_MANAGER.getModulePriority(this));

        if (!placeTimer.hasTimeElapsed(1000.0f - placeSpeed.getValue().floatValue() * 50.0f)) return;
        if (!sequential && placedSequentially) {
            placedSequentially = false;
            return;
        }

        placeRunnable = () -> {
            boolean switched = false;

            if (rotate.getValue().equalsIgnoreCase("Packet")) Lyrica.ROTATION_MANAGER.packetRotate(RotationUtils.getRotations(Vec3d.ofCenter(position, 1)));

            if (mc.player.getMainHandStack().getItem() != Items.END_CRYSTAL && mc.player.getOffHandStack().getItem() != Items.END_CRYSTAL) {
                InventoryUtils.switchSlot(autoSwitch.getValue(), slot, previousSlot);
                switched = true;
            }

            place(position);

            if (switched) {
                InventoryUtils.switchBack(autoSwitch.getValue(), slot, previousSlot);
            }

            if (godSync.getValue()) {
                boolean flag = !antiKick.getValue() || !(mc.player.getMainHandStack().getItem() instanceof ExperienceBottleItem) && !(mc.player.getOffHandStack().getItem() instanceof ExperienceBottleItem) && !Lyrica.MODULE_MANAGER.getModule(ThrowXPModule.class).isToggled();
                if ((!antiKick.getValue() || kickTicks > kickThreshold.getValue().intValue()) && flag) {
                    if (!fast.getValue()) {
                        for (Entity entity : mc.world.getEntities()) {
                            if (entity.getId() <= highestID) continue;
                            highestID = entity.getId();
                        }
                    }

                    for (int i = 1 - offset.getValue().intValue(); i < predictions.getValue().intValue(); ++i) {
                        Entity entity = mc.world.getEntityById(highestID);
                        if (entity == null || entity instanceof EndCrystalEntity) {
                            int id = highestID + i;

                            PlayerInteractEntityC2SPacket packet = PlayerInteractEntityC2SPacket.attack(mc.player, mc.player.isSneaking());
                            try {
                                Field field = PlayerInteractEntityC2SPacket.class.getDeclaredField(FabricLoader.getInstance().isDevelopmentEnvironment() ? "entityId" : "field_12870");
                                field.setAccessible(true);
                                field.setInt(packet, id);
                            } catch (Exception ignored) {
                            }

                            mc.getNetworkHandler().sendPacket(packet);
                            if (godSwing.getValue().equals("Strict")) mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));

                            attackedCrystals.put(id, System.currentTimeMillis());
                        }
                    }

                    if (godSwing.getValue().equals("Normal")) mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                }

                kickTicks++;
            }
        };

        if (sequential) {
            placeRunnable.run();
            placeRunnable = null;

            placedSequentially = true;
        }
    }

    private EndCrystalEntity calculateCrystals() {
        if (!attack.getValue()) return null;
        if (shouldPause("Attack")) return null;

        List<PlayerEntity> players = getPlayers();
        if (players.isEmpty()) return null;

        EndCrystalEntity optimalCrystal = null;
        float optimalDamage = 0.0f;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof EndCrystalEntity crystal)) continue;
            if (!crystal.isAlive()) continue;
            if (inhibit.getValue() && attackedCrystals.containsKey(entity.getId())) continue;
            if (crystal.getBoundingBox().squaredMagnitude(mc.player.getEyePos()) > MathHelper.square(attackRange.getValue().doubleValue())) continue;
            if (!mc.world.getWorldBorder().contains(crystal.getBlockPos())) continue;
            if (!WorldUtils.canSee(crystal) && (raytrace.getValue() || crystal.getBoundingBox().squaredMagnitude(mc.player.getEyePos()) > MathHelper.square(attackWallsRange.getValue().doubleValue())))
                continue;

            if (!Lyrica.MODULE_MANAGER.getModule(SuicideModule.class).isToggled()) {
                float damage = DamageUtils.getCrystalDamage(mc.player, null, crystal, ignoreTerrain.getValue());
                if (damage > maximumSelfDamage.getValue().floatValue()) continue;
                if (antiSuicide.getValue() && damage > mc.player.getHealth() + mc.player.getAbsorptionAmount()) continue;
            }

            boolean override = false;
            for (PlayerEntity player : players) {
                float damage = DamageUtils.getCrystalDamage(player, PositionUtils.extrapolate(player, extrapolation.getValue().intValue()), crystal, ignoreTerrain.getValue());
                if (damage < getMinimumDamage(player, minimumDamage.getValue().floatValue()) && damage < player.getHealth() + player.getAbsorptionAmount() && !(damage * (1.0f + lethalMultiplier.getValue().floatValue()) >= player.getHealth() + player.getAbsorptionAmount()))
                    continue;

                if (damage > optimalDamage || damage > player.getHealth() + player.getAbsorptionAmount()) {
                    optimalCrystal = crystal;
                    optimalDamage = damage;

                    if (damage > player.getHealth() + player.getAbsorptionAmount()) {
                        override = true;
                        break;
                    }
                }
            }

            if (override) break;
        }

        return optimalCrystal;
    }

    // Parallel version of crystal calculation using available CPU cores.
    private EndCrystalEntity calculateCrystalsParallel() {
        if (!attack.getValue()) return null;
        if (shouldPause("Attack")) return null;

        List<PlayerEntity> players = getPlayers();
        if (players.isEmpty()) return null;

        // Snapshot crystal list to avoid ConcurrentModification while parallel.
        List<EndCrystalEntity> crystals;
        try {
            crystals = StreamSupport.stream(mc.world.getEntities().spliterator(), false)
                .filter(e -> e instanceof EndCrystalEntity && e != null)
                .map(e -> (EndCrystalEntity) e)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        } catch (Exception e) {
            return null;
        }

        if (crystals.isEmpty()) return null;

        AtomicReference<EndCrystalEntity> bestCrystal = new AtomicReference<>(null);
        AtomicReference<Float> bestDamage = new AtomicReference<>(0.0f);
        
        List<EndCrystalEntity> finalCrystals = crystals;
        try {
            finalCrystals.parallelStream().forEach(crystal -> {
                if (crystal == null || !crystal.isAlive()) return;
                if (inhibit.getValue() && attackedCrystals.containsKey(crystal.getId())) return;
                if (crystal.getBoundingBox().squaredMagnitude(mc.player.getEyePos()) > MathHelper.square(attackRange.getValue().doubleValue())) return;
                if (!mc.world.getWorldBorder().contains(crystal.getBlockPos())) return;
                if (!WorldUtils.canSee(crystal) && (raytrace.getValue() || crystal.getBoundingBox().squaredMagnitude(mc.player.getEyePos()) > MathHelper.square(attackWallsRange.getValue().doubleValue())))
                    return;

                if (!Lyrica.MODULE_MANAGER.getModule(SuicideModule.class).isToggled()) {
                    float selfDmg = DamageUtils.getCrystalDamage(mc.player, null, crystal, ignoreTerrain.getValue());
                    if (selfDmg > maximumSelfDamage.getValue().floatValue()) return;
                    if (antiSuicide.getValue() && selfDmg > mc.player.getHealth() + mc.player.getAbsorptionAmount()) return;
                }

                for (PlayerEntity player : players) {
                    float damage = DamageUtils.getCrystalDamage(player, PositionUtils.extrapolate(player, extrapolation.getValue().intValue()), crystal, ignoreTerrain.getValue());
                    if (damage < getMinimumDamage(player, minimumDamage.getValue().floatValue()) && damage < player.getHealth() + player.getAbsorptionAmount() && !(damage * (1.0f + lethalMultiplier.getValue().floatValue()) >= player.getHealth() + player.getAbsorptionAmount()))
                        continue;

                    synchronized (bestCrystal) {
                        if (damage > bestDamage.get()) {
                            bestDamage.set(damage);
                            bestCrystal.set(crystal);
                        }
                    }
                }
            });
        } catch (Exception e) {
            return null;
        }

        return bestCrystal.get();
    }

    private PlaceTarget calculatePlacements(BlockPos exception) {
        if (!place.getValue()) return null;

        if (shouldPause("Place") || ((autoSwitch.getValue().equalsIgnoreCase("None") || InventoryUtils.findHotbar(Items.END_CRYSTAL) == -1) && (mc.player.getMainHandStack().getItem() != Items.END_CRYSTAL && mc.player.getOffHandStack().getItem() != Items.END_CRYSTAL)))
            return null;

        List<PlayerEntity> players = getPlayers();
        if (players.isEmpty()) return null;

        BlockPos optimalPosition = null;
        PlayerEntity optimalPlayer = null;
        List<Entity> obstructions = new ArrayList<>();
        float optimalDamage = 0.0f;

        int calculations = 0;

        for (int i = 0; i < Lyrica.WORLD_MANAGER.getRadius(Math.max(placeRange.getValue().doubleValue(), placeWallsRange.getValue().doubleValue())); i++) {
            BlockPos position = mc.player.getBlockPos().add(Lyrica.WORLD_MANAGER.getOffset(i));

            if (mc.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter(position)) > MathHelper.square(placeRange.getValue().doubleValue())) continue;
            if (!mc.world.getWorldBorder().contains(position)) continue;
            if (mc.world.getBlockState(position).getBlock() != Blocks.OBSIDIAN && mc.world.getBlockState(position).getBlock() != Blocks.BEDROCK) continue;
            if (!mc.world.getBlockState(position.add(0, 1, 0)).isAir() || (placements.getValue().equalsIgnoreCase("Protocol") && !mc.world.getBlockState(position.add(0, 2, 0)).isAir())) continue;

            if (!WorldUtils.canSee(position) && (raytrace.getValue() || mc.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter(position)) > MathHelper.square(placeWallsRange.getValue().doubleValue()))) continue;
            if (mc.world.getOtherEntities(null, new Box(position.add(0, 1, 0))).stream().anyMatch(entity -> entity.isAlive() && !(entity instanceof ExperienceOrbEntity) && !(entity instanceof EndCrystalEntity))) continue;

            List<Entity> obstructingCrystals = mc.world.getOtherEntities(null, new Box(position.add(0, 1, 0))).stream().filter(entity -> entity instanceof EndCrystalEntity crystal && crystal.age >= (20 - attackSpeed.getValue().intValue()) + 15).toList();

            if (!Lyrica.MODULE_MANAGER.getModule(SuicideModule.class).isToggled()) {
                float selfDamage = DamageUtils.getCrystalDamage(mc.player, null, position, exception, ignoreTerrain.getValue());
                if (selfDamage > maximumSelfDamage.getValue().floatValue()) continue;
                if (antiSuicide.getValue() && selfDamage > mc.player.getHealth() + mc.player.getAbsorptionAmount()) continue;
            }

            boolean override = false;
            for (PlayerEntity player : players) {
                calculations++;

                float damage = DamageUtils.getCrystalDamage(player, PositionUtils.extrapolate(player, extrapolation.getValue().intValue()), position, exception, ignoreTerrain.getValue());
                if (damage < getMinimumDamage(player, minimumDamage.getValue().floatValue()) && damage < player.getHealth() + player.getAbsorptionAmount() && !(damage * (1.0f + lethalMultiplier.getValue().floatValue()) >= player.getHealth() + player.getAbsorptionAmount()))
                    continue;

                if (exception == null && !obstructingCrystals.isEmpty()) {
                    obstructions.add(obstructingCrystals.getFirst());
                    break;
                }

                if (damage > optimalDamage || damage > player.getHealth() + player.getAbsorptionAmount()) {
                    optimalPosition = position;
                    optimalPlayer = player;
                    optimalDamage = damage;

                    if (damage > player.getHealth() + player.getAbsorptionAmount()) {
                        override = true;
                        break;
                    }
                }
            }

            if (override) break;
        }

        if (optimalPosition == null) return new PlaceTarget(null, null, obstructions, null, 0.0f, calculations);
        return new PlaceTarget(optimalPosition, optimalPlayer, obstructions, exception, optimalDamage, calculations);
    }

    private PlaceTarget calculatePlacementsParallel(BlockPos exception) {
        if (!place.getValue()) return null;

        if (shouldPause("Place") || ((autoSwitch.getValue().equalsIgnoreCase("None") || InventoryUtils.findHotbar(Items.END_CRYSTAL) == -1) && (mc.player.getMainHandStack().getItem() != Items.END_CRYSTAL && mc.player.getOffHandStack().getItem() != Items.END_CRYSTAL)))
            return null;

        List<PlayerEntity> players = getPlayers();
        if (players.isEmpty()) return null;

        int radius = Lyrica.WORLD_MANAGER.getRadius(Math.max(placeRange.getValue().doubleValue(), placeWallsRange.getValue().doubleValue()));

        AtomicReference<BlockPos> bestPos = new AtomicReference<>(null);
        AtomicReference<PlayerEntity> bestPlayer = new AtomicReference<>(null);
        AtomicReference<Float> bestDmg = new AtomicReference<>(0.0f);
        AtomicReference<List<Entity>> bestObs = new AtomicReference<>(new ArrayList<>());
        AtomicInteger calcCount = new AtomicInteger(0);

        try {
            IntStream.range(0, radius).parallel().forEach(i -> {
                try {
                    BlockPos position = mc.player.getBlockPos().add(Lyrica.WORLD_MANAGER.getOffset(i));
                    if (position == null) return;

                    if (mc.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter(position)) > MathHelper.square(placeRange.getValue().doubleValue())) return;
                    if (!mc.world.getWorldBorder().contains(position)) return;
                    if (mc.world.getBlockState(position).getBlock() != Blocks.OBSIDIAN && mc.world.getBlockState(position).getBlock() != Blocks.BEDROCK) return;
                    if (!mc.world.getBlockState(position.add(0, 1, 0)).isAir() || (placements.getValue().equalsIgnoreCase("Protocol") && !mc.world.getBlockState(position.add(0, 2, 0)).isAir())) return;
                    if (!WorldUtils.canSee(position) && (raytrace.getValue() || mc.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter(position)) > MathHelper.square(placeWallsRange.getValue().doubleValue()))) return;
                    
                    // Thread-safe obstructions kontrolü
                    List<Entity> entities = new ArrayList<>();
                    try {
                        entities = mc.world.getOtherEntities(null, new Box(position.add(0, 1, 0)))
                            .stream()
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    } catch (Exception e) {
                        // Entity işleme hatası olursa devam et
                        return;
                    }
                    
                    // Varlık kontrolü
                    if (entities.stream().anyMatch(entity -> entity != null && entity.isAlive() && !(entity instanceof ExperienceOrbEntity) && !(entity instanceof EndCrystalEntity))) 
                        return;

                    List<Entity> obstructing = new ArrayList<>();
                    try {
                        obstructing = mc.world.getOtherEntities(null, new Box(position.add(0, 1, 0)))
                            .stream()
                            .filter(entity -> entity instanceof EndCrystalEntity crystal && crystal.age >= (20 - attackSpeed.getValue().intValue()) + 15)
                            .collect(Collectors.toList());
                    } catch (Exception e) {
                        // Entity işleme hatası olursa devam et
                        return;
                    }

                    if (!Lyrica.MODULE_MANAGER.getModule(SuicideModule.class).isToggled()) {
                        float selfDamage = DamageUtils.getCrystalDamage(mc.player, null, position, exception, ignoreTerrain.getValue());
                        if (selfDamage > maximumSelfDamage.getValue().floatValue()) return;
                        if (antiSuicide.getValue() && selfDamage > mc.player.getHealth() + mc.player.getAbsorptionAmount()) return;
                    }

                    for (PlayerEntity player : players) {
                        if (player == null) continue;
                        
                        calcCount.incrementAndGet();
                        float dmg = DamageUtils.getCrystalDamage(player, PositionUtils.extrapolate(player, extrapolation.getValue().intValue()), position, exception, ignoreTerrain.getValue());
                        if (dmg < getMinimumDamage(player, minimumDamage.getValue().floatValue()) && dmg < player.getHealth() + player.getAbsorptionAmount() && !(dmg * (1.0f + lethalMultiplier.getValue().floatValue()) >= player.getHealth() + player.getAbsorptionAmount()))
                            continue;

                        synchronized (bestPos) {
                            if (dmg > bestDmg.get()) {
                                bestDmg.set(dmg);
                                bestPos.set(position);
                                bestPlayer.set(player);
                                bestObs.set(new ArrayList<>(obstructing)); // Thread-safe kopya oluşturma
                            }
                        }
                    }
                } catch (Exception e) {
                    // Herhangi bir iç hata olursa, bu pozisyonu atla
                }
            });
        } catch (Exception e) {
            // Tüm paralel işlemde bir hata oluşursa boş döndür
            return new PlaceTarget(null, null, new ArrayList<>(), exception, 0.0f, calcCount.get());
        }

        if (bestPos.get() == null) return new PlaceTarget(null, null, bestObs.get(), exception, 0.0f, calcCount.get());
        return new PlaceTarget(bestPos.get(), bestPlayer.get(), bestObs.get(), exception, bestDmg.get(), calcCount.get());
    }

    private float[] calculateRotations(Vec3d vec3d) {
        float[] rotations = RotationUtils.getRotations(vec3d);

        if (yawStep.getValue()) {
            float yaw;

            float difference = Lyrica.ROTATION_MANAGER.getServerYaw() - rotations[0];
            if (Math.abs(difference) > 180.0f) difference += difference > 0.0f ? -360.0f : 360.0f;

            float deltaYaw = (difference > 0.0f ? -1 : 1) * yawStepThreshold.getValue().floatValue();

            if (Math.abs(difference) > yawStepThreshold.getValue().floatValue()) yaw = Lyrica.ROTATION_MANAGER.getServerYaw() + deltaYaw;
            else yaw = rotations[0];

            rotations[0] = yaw;
        }

        return rotations;
    }

    private void attack(EndCrystalEntity crystal) {
        int previousSlot = mc.player.getInventory().selectedSlot;
        boolean switched = false;

        if (!antiWeakness.getValue().equalsIgnoreCase("None") && mc.player.hasStatusEffect(StatusEffects.WEAKNESS)) {
            int slot = InventoryUtils.findBestSword(InventoryUtils.HOTBAR_START, InventoryUtils.HOTBAR_END);
            if (slot != -1) {
                InventoryUtils.switchSlot(antiWeakness.getValue(), slot, previousSlot);
                switched = true;
            }
        }

        mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(crystal, mc.player.isSneaking()));
        mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));

        if (switched) {
            InventoryUtils.switchBack(antiWeakness.getValue(), 0, previousSlot);
        }

        attackedCrystals.put(crystal.getId(), System.currentTimeMillis());
        attackTimer.reset();
    }

    private void place(BlockPos position) {
        Hand hand = mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL ? Hand.OFF_HAND : Hand.MAIN_HAND;
        Direction direction = WorldUtils.getClosestDirection(position, true);

        mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(hand, new BlockHitResult(WorldUtils.getHitVector(position, direction), direction, position, false), 0));

        switch (swing.getValue()) {
            case "Default" -> mc.player.swingHand(hand);
            case "Packet" -> mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(hand));
            case "Mainhand" -> mc.player.swingHand(Hand.MAIN_HAND);
            case "Offhand" -> mc.player.swingHand(Hand.OFF_HAND);
            case "Both" -> {
                mc.player.swingHand(Hand.MAIN_HAND);
                mc.player.swingHand(Hand.OFF_HAND);
            }
        }

        placedCrystals.put(position, System.currentTimeMillis());
        countedCrystals.put(position, System.currentTimeMillis());
        placeTimer.reset();
    }

    private List<PlayerEntity> getPlayers() {
        List<PlayerEntity> players = new ArrayList<>();
        if (Lyrica.MODULE_MANAGER.getModule(SuicideModule.class).isToggled()) {
            players.add(mc.player);
            return players;
        }

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;
            if (!player.isAlive()) continue;
            if (mc.player.squaredDistanceTo(player) > MathHelper.square(enemyRange.getValue().doubleValue())) continue;
            if (Lyrica.FRIEND_MANAGER.contains(player.getName().getString())) continue;

            players.add(player);
        }

        return players;
    }

    private boolean shouldPause(String process) {
        boolean eatingFlag = (whileEating.getValue().equalsIgnoreCase("None") || (process.equalsIgnoreCase("Attack") && whileEating.getValue().equalsIgnoreCase("Place")) || (process.equalsIgnoreCase("Place") && whileEating.getValue().equalsIgnoreCase("Attack")));
        return eatingFlag && mc.player.isUsingItem();
    }

    private float getMinimumDamage(PlayerEntity player, float minimumDamage) {
        if (player == null) return minimumDamage;

        if (chestBreak.getValue() && mc.world.getOtherEntities(null, new Box(player.getBlockPos()).expand(1)).stream().anyMatch(entity -> entity instanceof ItemEntity item && item.getStack().getItem() == Items.OBSIDIAN && item.getStack().getCount() >= 8 && item.age <= 2 + Lyrica.SERVER_MANAGER.getPingDelay() + (20 - placeSpeed.getValue().intValue())) && !mc.world.getOtherEntities(null, new Box(mc.player.getBlockPos()).expand(1)).stream().anyMatch(entity -> entity instanceof ItemEntity item && item.getStack().getItem() == Items.OBSIDIAN && item.getStack().getCount() >= 8 && item.age <= 2 + Lyrica.SERVER_MANAGER.getPingDelay() + (20 - placeSpeed.getValue().intValue()))) return 2.0f;
        if (facePlaceMode.getValue().equalsIgnoreCase("None")) return minimumDamage;

        if (facePlaceSpeed.getValue().equalsIgnoreCase("Normal") || facePlaceTimer.hasTimeElapsed(facePlaceDelay.getValue().longValue() * 50L)) {
            if (facePlaceMode.getValue().equalsIgnoreCase("Always")) return Math.min(minimumDamage, 2.0f);
            if (facePlaceMode.getValue().equalsIgnoreCase("Dynamic") && healthPlace.getValue() && (player.getHealth() + player.getAbsorptionAmount()) <= health.getValue().floatValue()) return Math.min(minimumDamage, 2.0f);
            if (facePlaceMode.getValue().equalsIgnoreCase("Dynamic") && armorPlace.getValue()) {
                for (ItemStack stack : player.getArmorItems()) {
                    if (stack == null || !(stack.getItem() instanceof ArmorItem)) continue;
                    if (Math.round(((stack.getMaxDamage() - stack.getDamage()) * 100.0) / stack.getMaxDamage()) <= percentage.getValue().intValue()) {
                        return Math.min(minimumDamage, 2.0f);
                    }
                }
            }
        }

        return minimumDamage;
    }

    @Getter @AllArgsConstructor
    public static class PlaceTarget {
        private BlockPos position;
        private PlayerEntity player;
        private List<Entity> obstructions;
        private BlockPos exception;
        private float damage;
        private int calculations;

        public PlaceTarget clone() {
            return new PlaceTarget(position, player, obstructions, exception, damage, calculations);
        }
    }
}