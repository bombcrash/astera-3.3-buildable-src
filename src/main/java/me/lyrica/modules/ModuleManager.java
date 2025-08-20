package me.lyrica.modules;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import lombok.Getter;
import me.lyrica.Lyrica;
import me.lyrica.events.SubscribeEvent;
import me.lyrica.events.impl.KeyInputEvent;
import me.lyrica.events.impl.MouseInputEvent;
import me.lyrica.events.impl.UnfilteredKeyInputEvent;
import me.lyrica.events.impl.UnfilteredMouseInputEvent;
import me.lyrica.modules.impl.combat.*;
import me.lyrica.modules.impl.core.*;
import me.lyrica.modules.impl.debug.*;
import me.lyrica.modules.impl.miscellaneous.*;
import me.lyrica.modules.impl.movement.*;
import me.lyrica.modules.impl.movement.elytrafly.*;
import me.lyrica.modules.impl.player.*;
import me.lyrica.modules.impl.visuals.*;
import me.lyrica.settings.Setting;
import me.lyrica.settings.impl.BindSetting;
import me.lyrica.utils.IMinecraft;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Getter
public class ModuleManager implements IMinecraft {
    private final List<Module> modules = new ArrayList<>();
    private final Map<Class<? extends Module>, Module> moduleClasses = new Reference2ReferenceOpenHashMap<>();

    private static final String CLASS_HASH = calculateClassHash();
    private static String calculateClassHash() {
        try (InputStream is = ModuleManager.class.getResourceAsStream("/me/lyrica/modules/ModuleManager.class")) {
            if (is == null) return "missing";
            return DigestUtils.sha256Hex(is);
        } catch (Exception e) {
            return "error";
        }
    }

    public ModuleManager() {
        Lyrica.EVENT_HANDLER.subscribe(this);

        // Combat modülleri
        registerModule(new AutoArmorModule());
        registerModule(new AutoBedModule());
        registerModule(new AutoBowReleaseModule());
        registerModule(new AutoCrystalModule());
        registerModule(new AutoLogModule());
        registerModule(new AutoTotemModule());
        registerModule(new AutoTrapModule());
        registerModule(new AutoWebModule());
        registerModule(new BlockerModule());
        registerModule(new ChatMusic());
        registerModule(new CriticalsModule());
        registerModule(new ElRoboticoModule());
        registerModule(new HoleFillModule());
        registerModule(new KillAuraModule());
        registerModule(new KillSayModule());
        registerModule(new MaceAuraModule());
        registerModule(new NoHitDelayModule());
        registerModule(new SelfBowModule());
        registerModule(new SelfFillModule());
        registerModule(new SelfTrapModule());
        registerModule(new SuicideModule());
        registerModule(new SurroundModule());
        registerModule(new PearlBlocker());  //dayi yeni ekledik git bozuk
        registerModule(new TotemPopCounter());

        // Core modülleri
        registerModule(new BaritoneIntegration());
        registerModule(new CapesModule());
        registerModule(new ClickGuiModule());
        registerModule(new ColorModule());
        registerModule(new CommandsModule());
        registerModule(new FontModule());
        registerModule(new FriendModule());
        registerModule(new HUDModule());
        registerModule(new RendersModule());
        registerModule(new RotationsModule());
        registerModule(new RPCModule());
        registerModule(new SoundFX());

        // Debug modülleri
        registerModule(new Debug());
        registerModule(new ExpectionPatcher());
        registerModule(new InfoChecker());
        registerModule(new KickLogger());
        registerModule(new PacketLogger());
        registerModule(new Rubberband());

        // Miscellaneous modülleri
        registerModule(new AntiPacketKickModule());
        registerModule(new AutoLoginModule());
        registerModule(new AutoReconnectModule());
        registerModule(new AutoRespawnModule());
        registerModule(new BetterChatModule());
        registerModule(new ChatSuffix());
        registerModule(new ExtraTabModule());
        registerModule(new FakePlayerModule());
        registerModule(new FastLatencyModule());
        registerModule(new FOVModifierModule());
        registerModule(new GhastFarmer());
        registerModule(new InventoryCleanerModule());
        registerModule(new MouseFixModule());
        registerModule(new NameProtectModule());
        registerModule(new NoSoundLagModule());
        registerModule(new NotificationsModule());
        registerModule(new RandomizerModule());
        registerModule(new ShulkerInfoModule());
        registerModule(new SpammerModule());
        registerModule(new UnfocusedFPSModule());

        // Movement modülleri
        registerModule(new AccelerateModule());
        registerModule(new AntiVoidModule());
        registerModule(new AutoWalkModule());
        registerModule(new BlockPosX());
        registerModule(new DisablerModule());
        registerModule(new ElytraBoost());
        registerModule(new ElytraFlyModule());
        registerModule(new FakeLagModule());
        registerModule(new FastWebModule());
        registerModule(new HitboxDesyncModule());
        registerModule(new HoleSnapModule());
        registerModule(new HoleTpModule());
        registerModule(new HorizontalCollision());
        registerModule(new IceSpeedModule());
        registerModule(new InventoryControlModule());
        registerModule(new MassDestruction());
        registerModule(new NoFallModule());
        registerModule(new NoJumpDelayModule());
        registerModule(new NoSlowModule());
        registerModule(new PhaseModule());
        registerModule(new ReverseStepModule());
        registerModule(new SafeWalkModule());
        registerModule(new SpeedModule());
        registerModule(new SprintModule());
        registerModule(new StepModule());
        registerModule(new TickShiftModule());
        registerModule(new VelocityModule());

        // Player modülleri
        registerModule(new AirPlaceModule());
        registerModule(new AntiHungerModule());
        registerModule(new AntiNBT());
        registerModule(new FastPlaceModule());
        registerModule(new MultiTaskModule());
        registerModule(new NoEntityTraceModule());
        registerModule(new NoInteractModule());
        registerModule(new NoRotateModule());
        registerModule(new PingSpoofModule());
        registerModule(new ReachModule());
        registerModule(new ReplenishModule());
        registerModule(new SilentDouble());
        registerModule(new SpeedMineModule());
        registerModule(new SwingModule());
        registerModule(new ThrowFireworkModule());
        registerModule(new ThrowPearlModule());
        registerModule(new ThrowXPModule());
        registerModule(new TimerModule());
        registerModule(new VillageRoller());
        registerModule(new XCarryModule());

        // Visual modülleri
        registerModule(new AmbienceModule());
        registerModule(new ArrowTracersModule());
        registerModule(new AspectRatioModule());
        registerModule(new AtmosphereModule());
        registerModule(new BlockHighlightModule());
        registerModule(new BreakHighlightModule());
        registerModule(new ChamsModule());
        registerModule(new CrosshairModule());
        registerModule(new DeathEffectsModule());
        registerModule(new EntityModifierModule());
        registerModule(new ESPModule());
        registerModule(new FreecamModule());
        registerModule(new HandProgressModule());
        registerModule(new HoleESPModule());
        registerModule(new IconsModule());
        registerModule(new NameTagsModule());
        registerModule(new NoInterpolationModule());
        registerModule(new NoRenderModule());
        registerModule(new PhaseESPModule());
        registerModule(new PopChamsModule());
        registerModule(new SearchModule());
        registerModule(new ShadersModule());
        registerModule(new TextESPModule());
        registerModule(new TrajectoriesModule());
        registerModule(new TracersModule());
        registerModule(new ViewClipModule());
        registerModule(new ViewModelModule());
        registerModule(new VoidESPModule());
        registerModule(new WaypointsModule());

        modules.sort(Comparator.comparing(Module::getName));
    }
    
    private void registerModule(Module module) {
        try {
            // Modülün alanlarını tarayıp ayarları ekle
            for (Field field : module.getClass().getDeclaredFields()) {
                if (!Setting.class.isAssignableFrom(field.getType())) continue;
                if (!field.canAccess(module)) field.setAccessible(true);
                
                module.getSettings().add((Setting) field.get(module));
            }
            
            // Standart ayarları ekle
            module.getSettings().add(module.chatNotify);
            module.getSettings().add(module.drawn);
            module.getSettings().add(module.bind);
            
            // Modül listelerine ekle
            modules.add(module);
            moduleClasses.put(module.getClass(), module);
        } catch (Exception e) {
            Lyrica.LOGGER.error("Failed to register module: " + module.getClass().getName(), e);
        }
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {
        if (modules == null) return;
        modules.stream()
                .filter(m -> m != null && m.getBind() == event.getKey())
                .forEach(m -> {
                    if (m.getBindMode() == BindSetting.BindMode.TOGGLE) {
                        m.setToggled(!m.isToggled());
                    } else if (m.getBindMode() == BindSetting.BindMode.HOLD && !m.isToggled()) {
                        m.setToggled(true);
                    }
                });
    }

    @SubscribeEvent
    public void onMouseInput(MouseInputEvent event) {
        if (modules == null) return;
        modules.stream()
                .filter(m -> m != null && m.getBind() == (-event.getButton() - 1))
                .forEach(m -> {
                    if (m.getBindMode() == BindSetting.BindMode.TOGGLE) {
                        m.setToggled(!m.isToggled());
                    } else if (m.getBindMode() == BindSetting.BindMode.HOLD && !m.isToggled()) {
                        m.setToggled(true);
                    }
                });
    }
    
    @SubscribeEvent
    public void onUnfilteredKeyInput(UnfilteredKeyInputEvent event) {
        if (modules == null) return;
        
        // GLFW_RELEASE = 0
        if (event.getAction() == 0) {
            modules.stream()
                    .filter(m -> m != null && m.getBind() == event.getKey() && m.getBindMode() == BindSetting.BindMode.HOLD && m.isToggled())
                    .forEach(m -> m.setToggled(false));
        }
    }
    
    @SubscribeEvent
    public void onUnfilteredMouseInput(UnfilteredMouseInputEvent event) {
        if (modules == null) return;
        
        // GLFW_RELEASE = 0
        if (event.getAction() == 0) {
            modules.stream()
                    .filter(m -> m != null && m.getBind() == (-event.getButton() - 1) && m.getBindMode() == BindSetting.BindMode.HOLD && m.isToggled())
                    .forEach(m -> m.setToggled(false));
        }
    }

    public List<Module> getModules(Module.Category category) {
        if (modules == null) return new ArrayList<>();
        return modules.stream().filter(m -> m != null && m.getCategory() == category).toList();
    }

    public Module getModule(String name) {
        if (modules == null || name == null) return null;
        return modules.stream().filter(m -> m != null && m.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    @SuppressWarnings("unchecked")
    public <T extends Module> T getModule(Class<T> clazz) {
        if (moduleClasses == null || clazz == null) return null;
        return (T) moduleClasses.get(clazz);
    }
}
