package io.github.amerebagatelle.fabricskyboxes;

import io.github.amerebagatelle.fabricskyboxes.fabricapi.client.keybinding.v1.KeyBindingHelper;
import io.github.amerebagatelle.fabricskyboxes.fabricapi.event.lifecycle.v1.ClientTickEvents;
import io.github.amerebagatelle.fabricskyboxes.fabricapi.resource.ResourceManagerHelper;
import io.github.amerebagatelle.fabricskyboxes.resource.SkyboxResourceListener;
import io.github.amerebagatelle.fabricskyboxes.skyboxes.SkyboxType;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

@Mod(FabricSkyBoxesClient.MODID)
@OnlyIn(Dist.CLIENT)
public class FabricSkyBoxesClient {
    public static final String MODID = "fabricskyboxes";
    private static Logger LOGGER;
    private static KeyBinding toggleFabricSkyBoxes;

    public static Logger getLogger() {
        if (LOGGER == null) {
            LOGGER = LogManager.getLogger("FabricSkyboxes");
        }
        return LOGGER;
    }

    public FabricSkyBoxesClient() {
        IEventBus MOD_BUS = FMLJavaModLoadingContext.get().getModEventBus();
        MOD_BUS.addListener(this::onInitializeClient);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void onInitializeClient(final FMLClientSetupEvent event) {
        SkyboxType.initRegistry();
        toggleFabricSkyBoxes = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.fabricskyboxes.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_KP_0, "category.fabricskyboxes"));
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SkyboxResourceListener());
        ClientTickEvents.END_WORLD_TICK.register(SkyboxManager.getInstance());
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleFabricSkyBoxes.wasPressed()) {
                SkyboxManager.getInstance().setEnabled(!SkyboxManager.getInstance().isEnabled());
                assert client.player != null;
                if (SkyboxManager.getInstance().isEnabled()) {
                    client.player.sendMessage(Text.translatable("fabricskyboxes.message.enabled"), false);
                } else {
                    client.player.sendMessage(Text.translatable("fabricskyboxes.message.disabled"), false);
                }
            }
        });
    }
}
