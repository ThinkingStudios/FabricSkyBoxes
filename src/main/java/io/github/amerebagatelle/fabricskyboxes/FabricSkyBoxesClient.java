package io.github.amerebagatelle.fabricskyboxes;

import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.portinglab.forgedfabricapi.resource.ResourceManagerHelper;
import io.github.amerebagatelle.fabricskyboxes.resource.SkyboxResourceListener;
import io.github.amerebagatelle.fabricskyboxes.skyboxes.SkyboxType;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

@Mod(FabricSkyBoxesClient.MODID)
@OnlyIn(Dist.CLIENT)
public class FabricSkyBoxesClient {
    public static final String MODID = "fabricskyboxes";
    private static Logger LOGGER;
    private static final KeyBinding toggleFabricSkyBoxes = new KeyBinding("key.fabricskyboxes.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_KP_0, "category.fabricskyboxes");

    public static Logger getLogger() {
        if (LOGGER == null) {
            LOGGER = LogManager.getLogger("FabricSkyboxes");
        }
        return LOGGER;
    }

    //public FabricSkyBoxesClient() {
    //    IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
    //    modEventBus.addListener(this::onInitializeClient);
    //    MinecraftForge.EVENT_BUS.register(this);
    //}

    public FabricSkyBoxesClient() {
        SkyboxType.initRegistry();
        KeyMappingRegistry.register(toggleFabricSkyBoxes);
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SkyboxResourceListener());
        ClientTickEvent.CLIENT_LEVEL_POST.register(SkyboxManager.getInstance());
        ClientTickEvent.CLIENT_POST.register(client -> {
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
