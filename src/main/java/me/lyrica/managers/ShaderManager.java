package me.lyrica.managers;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import me.lyrica.Lyrica;
import me.lyrica.mixins.accessors.*;
import me.lyrica.utils.IMinecraft;
import net.minecraft.client.gl.*;
import net.minecraft.client.render.*;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.util.Identifier;
import me.lyrica.modules.impl.visuals.ShadersModule;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Getter
public class ShaderManager implements IMinecraft {
    private final OutlineVertexConsumerProvider vertexConsumerProvider = new OutlineVertexConsumerProvider(VertexConsumerProvider.immediate(new BufferAllocator(256)));
    private final Framebuffer framebuffer;

    private final RenderPhase.Target target;
    private final Function<RenderPhase.TextureBase, RenderLayer> layerCreator;

    public ShaderManager() {
        framebuffer = new SimpleFramebuffer(mc.getWindow().getFramebufferWidth(), mc.getWindow().getFramebufferHeight(), true);
        framebuffer.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        target = new RenderPhase.Target("shader_target", () -> framebuffer.beginWrite(false), () -> mc.getFramebuffer().beginWrite(false));
        layerCreator = memoizeTexture(texture -> RenderLayer.of("lyrica_overlay", VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS, 1536, RenderLayer.MultiPhaseParameters.builder().program(RenderPhase.OUTLINE_PROGRAM).texture(texture).depthTest(RenderPhase.ALWAYS_DEPTH_TEST).target(target).build(RenderLayer.OutlineMode.IS_OUTLINE)));
    }

    public void prepare() {
        framebuffer.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        framebuffer.clear();

        mc.getFramebuffer().beginWrite(false);
    }

    public void render(int renderMode, float opacity) {
        String shaderName = "outline";
        ShadersModule shadersModule = null;
        if (Lyrica.MODULE_MANAGER != null) {
            shadersModule = Lyrica.MODULE_MANAGER.getModule(ShadersModule.class);
        }
        PostEffectProcessor shader = mc.getShaderLoader().loadPostEffect(Identifier.of("lyrica", shaderName), DefaultFramebufferSet.MAIN_ONLY);
        
        if (shader == null) {
            Lyrica.LOGGER.error("Shader is null when trying to render " + shaderName + " effect!");
            return;
        }
        
        ShaderProgram program = ((PostEffectProcessorAccessor) shader).getPasses().getFirst().getProgram();

        if (program == null) {
            System.err.println("Shader program 'lyrica:program/bloom' yüklenemedi!");
            return;
        }

        program.addSamplerTexture("DiffuseSampler", framebuffer.getColorAttachment());

        // Güvenli uniform ayarlama
        Uniform uniform;

        uniform = program.getUniform("RenderMode");
        if (uniform != null) uniform.set(renderMode);

        uniform = program.getUniform("FillOpacity");
        if (uniform != null) uniform.set(opacity);

        if ("outline".equals(shaderName) && shadersModule != null) {
            uniform = program.getUniform("lineWidth");
            if (uniform != null) uniform.set(shadersModule.lineWidth.getValue().floatValue());

            // Glow uniformları
            uniform = program.getUniform("GlowRadius");
            if (uniform != null) uniform.set(shadersModule.glowRadius.getValue().floatValue());

            uniform = program.getUniform("GlowIntensity");
            if (uniform != null) uniform.set(shadersModule.glowIntensity.getValue().floatValue());

            uniform = program.getUniform("GlowExponent");
            if (uniform != null) uniform.set(shadersModule.glowExponent.getValue().floatValue());

            uniform = program.getUniform("OutlineStrength");
            if (uniform != null) uniform.set(shadersModule.outlineStrength.getValue().floatValue());
        }

        shader.render(framebuffer, ((GameRendererAccessor) mc.gameRenderer).getPool());
        mc.getFramebuffer().beginWrite(false);

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE);

        framebuffer.drawInternal(mc.getWindow().getFramebufferWidth(), mc.getWindow().getFramebufferHeight());

        RenderSystem.disableBlend();
    }

    public void resize(int width, int height) {
        if (framebuffer != null) framebuffer.resize(width, height);
    }

    public VertexConsumerProvider create(VertexConsumerProvider parent, Color color) {
        return layer -> {
            VertexConsumer parentBuffer = parent.getBuffer(layer);

            if (!(layer instanceof RenderLayer.MultiPhase) || ((RenderLayerMultiPhaseParametersAccessor) (Object) ((RenderLayerMultiPhaseAccessor) layer).invokeGetPhases()).getOutlineMode() == RenderLayer.OutlineMode.NONE) {
                return parentBuffer;
            }

            vertexConsumerProvider.setColor(color.getRed(), color.getGreen(), color.getBlue(), 255);

            VertexConsumer outlineBuffer = vertexConsumerProvider.getBuffer(layerCreator.apply(((RenderLayerMultiPhaseParametersAccessor) (Object) ((RenderLayerMultiPhaseAccessor) layer).invokeGetPhases()).getTexture()));
            return outlineBuffer != null ? VertexConsumers.union(outlineBuffer, parentBuffer) : parentBuffer;
        };
    }

    private Function<RenderPhase.TextureBase, RenderLayer> memoizeTexture(Function<RenderPhase.TextureBase, RenderLayer> function) {
        return new Function<>() {
            private final Map<Identifier, RenderLayer> cache = new HashMap<>();

            public RenderLayer apply(RenderPhase.TextureBase texture) {
                return this.cache.computeIfAbsent(((RenderPhaseTextureBaseAccessor) texture).invokeGetId().get(), id -> function.apply(texture));
            }
        };
    }
}