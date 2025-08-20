package me.lyrica.modules.impl.core;

import lombok.Getter;
import me.lyrica.Lyrica;
import me.lyrica.modules.Module;
import me.lyrica.modules.RegisterModule;
import net.minecraft.util.Identifier;

@Getter
@RegisterModule(name = "Capes", description = "Applies the lyrica_win cape to yourself and to other users.", category = Module.Category.CORE, toggled = true, drawn = false)
public class CapesModule extends Module {
    public CapesModule() {
        this.capeTexture = Identifier.of(Lyrica.MOD_ID, "textures/cape.png");
    }

    private final Identifier capeTexture;
}
