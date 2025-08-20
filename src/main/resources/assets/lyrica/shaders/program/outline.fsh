#version 330

#define SEARCH_RADIUS 20.0
#define STEP 3.0        

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;
uniform int RenderMode;
uniform float FillOpacity;
uniform float lineWidth;

uniform float GlowRadius;
uniform float GlowIntensity;
uniform float GlowExponent;
uniform float OutlineStrength;

out vec4 fragColor;

void main() {
    vec4 current = texture(DiffuseSampler, texCoord);

    if (current.a != 0.0) {
        if (RenderMode == 1) discard;
        fragColor = vec4(current.rgb, current.a * FillOpacity);
    } else {
        if (RenderMode == 0) discard;

        float minDist = 9999.0;
        vec3 nearestColor = vec3(1.0);

        for (float x = -SEARCH_RADIUS; x <= SEARCH_RADIUS; x += STEP) {
            for (float y = -SEARCH_RADIUS; y <= SEARCH_RADIUS; y += STEP) {
                vec2 offset = vec2(x, y) * oneTexel;
                vec4 sample = texture(DiffuseSampler, texCoord + offset);
                if (sample.a == 0.0) continue;

                float dist = length(vec2(x, y));
                if (dist < minDist) {
                    minDist = dist;
                    nearestColor = sample.rgb;
                }
            }
        }

        float outlineEdge = 1.0 - smoothstep(0.5, 1.2, minDist);

        float glowFactor = 1.0 - smoothstep(0.0, GlowRadius, minDist);
        glowFactor = pow(glowFactor, GlowExponent);

        float glowAlpha  = clamp(glowFactor * GlowIntensity, 0.0, 1.0);

        // Sadece parıltı (glow) efekti
        vec3 glowColor = mix(nearestColor, vec3(1.0), 0.65) * glowAlpha;
        fragColor = vec4(glowColor, glowAlpha);
    }
}