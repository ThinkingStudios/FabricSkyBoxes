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

package org.portinglab.forgedfabricapi.impl.resource.loader;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import org.portinglab.forgedfabricapi.resource.ResourceManagerHelper;
import org.portinglab.forgedfabricapi.resource.IdentifiableResourceReloadListener;

public class ResourceManagerHelperImpl implements ResourceManagerHelper {
    private static final Map<ResourceType, ResourceManagerHelperImpl> registryMap = new HashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceManagerHelperImpl.class);

    private final Set<Identifier> addedListenerIds = new HashSet<>();
    private final Set<IdentifiableResourceReloadListener> addedListeners = new LinkedHashSet<>();

    public static ResourceManagerHelperImpl get(ResourceType type) {
        return registryMap.computeIfAbsent(type, (t) -> new ResourceManagerHelperImpl());
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