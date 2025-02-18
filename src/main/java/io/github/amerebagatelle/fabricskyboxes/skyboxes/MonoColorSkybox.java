package io.github.amerebagatelle.fabricskyboxes.skyboxes;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.amerebagatelle.fabricskyboxes.mixin.skybox.WorldRendererAccess;
import io.github.amerebagatelle.fabricskyboxes.util.object.Conditions;
import io.github.amerebagatelle.fabricskyboxes.util.object.Decorations;
import io.github.amerebagatelle.fabricskyboxes.util.object.Properties;
import io.github.amerebagatelle.fabricskyboxes.util.object.RGBA;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

public class MonoColorSkybox extends AbstractSkybox {
    public static Codec<MonoColorSkybox> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Properties.CODEC.fieldOf("properties").forGetter(AbstractSkybox::getProperties),
            Conditions.CODEC.optionalFieldOf("conditions", Conditions.DEFAULT).forGetter(AbstractSkybox::getConditions),
            Decorations.CODEC.optionalFieldOf("decorations", Decorations.DEFAULT).forGetter(AbstractSkybox::getDecorations),
            RGBA.CODEC.optionalFieldOf("color", RGBA.DEFAULT).forGetter(MonoColorSkybox::getColor)
    ).apply(instance, MonoColorSkybox::new));
    public RGBA color;

    public MonoColorSkybox() {
    }

    public MonoColorSkybox(Properties properties, Conditions conditions, Decorations decorations, RGBA color) {
        super(properties, conditions, decorations);
        this.color = color;
    }

    @Override
    public SkyboxType<? extends AbstractSkybox> getType() {
        return SkyboxType.MONO_COLOR_SKYBOX;
    }

    @Override
    public void render(WorldRendererAccess worldRendererAccess, MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean thickFog) {
        if (this.alpha > 0) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();

            for (int i = 0; i < 6; ++i) {
                matrices.push();
                if (i == 1) {
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
                } else if (i == 2) {
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0F));
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
                } else if (i == 3) {
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180.0F));
                } else if (i == 4) {
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90.0F));
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90.0F));
                } else if (i == 5) {
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-90.0F));
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0F));
                }

                Matrix4f matrix4f = matrices.peek().getPositionMatrix();
                bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
                bufferBuilder.vertex(matrix4f, -100.0F, -100.0F, -100.0F).color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), this.alpha).next();
                bufferBuilder.vertex(matrix4f, -100.0F, -100.0F, 100.0F).color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), this.alpha).next();
                bufferBuilder.vertex(matrix4f, 100.0F, -100.0F, 100.0F).color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), this.alpha).next();
                bufferBuilder.vertex(matrix4f, 100.0F, -100.0F, -100.0F).color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), this.alpha).next();
                BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
                matrices.pop();
            }

            this.renderDecorations(worldRendererAccess, matrices, projectionMatrix, tickDelta, bufferBuilder, this.alpha);

            RenderSystem.disableBlend();
        }
    }

    public RGBA getColor() {
        return this.color;
    }
}
