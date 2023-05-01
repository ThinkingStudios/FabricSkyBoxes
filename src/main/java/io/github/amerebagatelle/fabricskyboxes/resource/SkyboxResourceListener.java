package io.github.amerebagatelle.fabricskyboxes.resource;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import io.github.amerebagatelle.fabricskyboxes.FabricSkyBoxesClient;
import io.github.amerebagatelle.fabricskyboxes.SkyboxManager;
import io.github.amerebagatelle.fabricskyboxes.skyboxes.AbstractSkybox;
import io.github.amerebagatelle.fabricskyboxes.skyboxes.SkyboxType;
import io.github.amerebagatelle.fabricskyboxes.util.JsonObjectWrapper;
import io.github.amerebagatelle.fabricskyboxes.util.object.internal.Metadata;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.InputStreamReader;
import java.util.Map;

public class SkyboxResourceListener implements SynchronousResourceReloader {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().setLenient().create();
    private static final JsonObjectWrapper objectWrapper = new JsonObjectWrapper(new JsonObject());

    private static AbstractSkybox parseSkyboxJson(Identifier id) {
        AbstractSkybox skybox;
        Metadata metadata;
        FabricSkyBoxesClient.getLogger().info("parseSkyboxJson for " + id);

        try {
            metadata = Metadata.CODEC.decode(JsonOps.INSTANCE, objectWrapper.getFocusedObject()).getOrThrow(false, System.err::println).getFirst();
        } catch (RuntimeException e) {
            FabricSkyBoxesClient.getLogger().warn("Skipping invalid skybox " + id.toString(), e);
            FabricSkyBoxesClient.getLogger().warn(objectWrapper.toString());
            return null;
        }

        FabricSkyBoxesClient.getLogger().info("decoded metadata for " + id);

        SkyboxType<? extends AbstractSkybox> type = SkyboxType.REGISTRY.get(metadata.getType());

        Preconditions.checkNotNull(type, "Unknown skybox type: " + metadata.getType().getPath().replace('_', '-'));
        if (metadata.getSchemaVersion() == 1)
        {
            Preconditions.checkArgument(type.isLegacySupported(), "Unsupported schema version '1' for skybox type " + type.getName());
            FabricSkyBoxesClient.getLogger().debug("Using legacy deserializer for skybox " + id.toString());
            skybox = type.instantiate();
            //noinspection ConstantConditions
            type.getDeserializer().getDeserializer().accept(objectWrapper, skybox);
        }
        else
        {
            FabricSkyBoxesClient.getLogger().info("getSchemaVersion for " + id);

            skybox = type.getCodec(metadata.getSchemaVersion())
                    .decode(JsonOps.INSTANCE, objectWrapper.getFocusedObject())
                    .getOrThrow(false, System.err::println).getFirst();
        }

        FabricSkyBoxesClient.getLogger().info("returning skybox for " + id);
        return skybox;
    }

    @Override
    public void reload(ResourceManager manager) {
        SkyboxManager skyboxManager = SkyboxManager.getInstance();

        // clear registered skyboxes on reload
        skyboxManager.clearSkyboxes();

        // load new skyboxes
        Map<Identifier, Resource> resources = manager.findResources("sky", identifier -> identifier.getPath().endsWith(".json"));

        resources.forEach((identifier, resource) -> {
            try {
                JsonObject json = GSON.fromJson(new InputStreamReader(resource.getInputStream()), JsonObject.class);
                skyboxManager.addSkybox(identifier, json);
            } catch (Exception e) {
                FabricSkyBoxesClient.getLogger().error("Error reading skybox " + identifier.toString());
                e.printStackTrace();
            }
        });
    }
}
