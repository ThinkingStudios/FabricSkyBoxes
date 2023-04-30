/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.amerebagatelle.fabricskyboxes.fabricapi.impl.resource.loader;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import org.portinglab.fabricloader.loader.api.ModContainer;

import io.github.amerebagatelle.fabricskyboxes.fabricapi.resource.ResourceManagerHelper;
import io.github.amerebagatelle.fabricskyboxes.fabricapi.resource.ResourcePackActivationType;
import io.github.amerebagatelle.fabricskyboxes.fabricapi.resource.IdentifiableResourceReloadListener;

public class ResourceManagerHelperImpl implements ResourceManagerHelper {
    private static final Map<ResourceType, ResourceManagerHelperImpl> registryMap = new HashMap<>();
    private static final Set<Pair<Text, ModNioResourcePack>> builtinResourcePacks = new HashSet<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceManagerHelperImpl.class);

    private final Set<Identifier> addedListenerIds = new HashSet<>();
    private final Set<IdentifiableResourceReloadListener> addedListeners = new LinkedHashSet<>();

    public static ResourceManagerHelperImpl get(ResourceType type) {
        return registryMap.computeIfAbsent(type, (t) -> new ResourceManagerHelperImpl());
    }

    /**
     * Registers a built-in resource pack. Internal implementation.
     *
     * @param id             the identifier of the resource pack
     * @param subPath        the sub path in the mod resources
     * @param container      the mod container
     * @param displayName    the display name of the resource pack
     * @param activationType the activation type of the resource pack
     * @return {@code true} if successfully registered the resource pack, else {@code false}
     * @see ResourceManagerHelper#registerBuiltinResourcePack(Identifier, ModContainer, Text, ResourcePackActivationType)
     * @see ResourceManagerHelper#registerBuiltinResourcePack(Identifier, ModContainer, ResourcePackActivationType)
     */
    public static boolean registerBuiltinResourcePack(Identifier id, String subPath, ModContainer container, Text displayName, ResourcePackActivationType activationType) {
        // Assuming the mod has multiple paths, we simply "hope" that the  file separator is *not* different across them
        List<Path> paths = container.getRootPaths();
        String separator = paths.get(0).getFileSystem().getSeparator();
        subPath = subPath.replace("/", separator);
        ModNioResourcePack resourcePack = ModNioResourcePack.create(id, displayName, container, subPath, ResourceType.CLIENT_RESOURCES, activationType);
        ModNioResourcePack dataPack = ModNioResourcePack.create(id, displayName, container, subPath, ResourceType.SERVER_DATA, activationType);
        if (resourcePack == null && dataPack == null) return false;

        if (resourcePack != null) {
            builtinResourcePacks.add(new Pair<>(displayName, resourcePack));
        }

        if (dataPack != null) {
            builtinResourcePacks.add(new Pair<>(displayName, dataPack));
        }

        return true;
    }

    /**
     * Registers a built-in resource pack. Internal implementation.
     *
     * @param id             the identifier of the resource pack
     * @param subPath        the sub path in the mod resources
     * @param container      the mod container
     * @param activationType the activation type of the resource pack
     * @return {@code true} if successfully registered the resource pack, else {@code false}
     * @see ResourceManagerHelper#registerBuiltinResourcePack(Identifier, ModContainer, ResourcePackActivationType)
     * @see ResourceManagerHelper#registerBuiltinResourcePack(Identifier, ModContainer, Text, ResourcePackActivationType)
     */
    public static boolean registerBuiltinResourcePack(Identifier id, String subPath, ModContainer container, ResourcePackActivationType activationType) {
        return registerBuiltinResourcePack(id, subPath, container, Text.literal(id.getNamespace() + "/" + id.getPath()), activationType);
    }

    @Override
    public void registerReloadListener(IdentifiableResourceReloadListener listener) {
        if (!addedListenerIds.add(listener.getFabricId())) {
            LOGGER.warn("Tried to register resource reload listener " + listener.getFabricId() + " twice!");
            return;
        }

        if (!addedListeners.add(listener)) {
            throw new RuntimeException("Listener with previously unknown ID " + listener.getFabricId() + " already in listener set!");
        }
    }
}