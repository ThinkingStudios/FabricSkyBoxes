package io.github.amerebagatelle.fabricskyboxes;

import io.github.amerebagatelle.fabricskyboxes.skyboxes.SkyboxType;
import io.github.amerebagatelle.fabricskyboxes.util.object.*;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.registry.Registry;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.joml.Vector3f;

@Mod("fabricskyboxes_test")
public class TestClientModInitializer {
    static final SkyboxType<TestSkybox> TYPE;
    static final Properties PROPS;
    static final Conditions CONDITIONS;
    static final Decorations DECORATIONS;

    static {
        TYPE = SkyboxType.Builder.create(
                TestSkybox.class,
                "an-entirely-hardcoded-skybox"
        ).add(2, TestSkybox.CODEC).build();
        DECORATIONS = new Decorations(
                PlayerScreenHandler.BLOCK_ATLAS_TEXTURE,
                SpriteAtlasTexture.PARTICLE_ATLAS_TEXTURE,
                true,
                true,
                false,
                Rotation.DEFAULT,
                Blend.DECORATIONS
        );
        CONDITIONS = new Conditions.Builder()
                .biomes(new Identifier("minecraft:plains"))
                .worlds(new Identifier("minecraft:overworld"))
                .weather(Weather.CLEAR)
                .yRanges(new MinMaxEntry(40, 120))
                .build();
        PROPS = new Properties.Builder()
                .changesFog()
                .rotates()
                .rotation(
                        new Rotation(
                                new Vector3f(0.1F, 0.0F, 0.1F),
                                new Vector3f(0.0F, 0.0F, 0.0F),
                                1
                        )
                )
                .maxAlpha(0.99F)
                .transitionInDuration(15)
                .transitionOutDuration(15)
                .fade(new Fade(1000, 2000, 11000, 12000, false))
                .build();
    }

    public TestClientModInitializer() {
        IEventBus MOD_BUS = FMLJavaModLoadingContext.get().getModEventBus();
        MOD_BUS.addListener(this::onInitializeClient);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void onInitializeClient(final FMLClientSetupEvent event) {
        Registry.register(SkyboxType.REGISTRY, TYPE.createId("test"), TYPE);
        SkyboxManager.getInstance().addPermanentSkybox(Identifier.of("fabricskyboxes_testmod", "test_skybox"), TestSkybox.INSTANCE);
    }
}
