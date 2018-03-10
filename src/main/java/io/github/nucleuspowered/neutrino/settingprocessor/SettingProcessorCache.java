/*
 * This file is part of Neutrino, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.neutrino.settingprocessor;

import com.google.common.collect.Maps;

import java.util.Map;

public class SettingProcessorCache {

    private SettingProcessorCache() {}

    private static Map<Class<? extends SettingProcessor>, SettingProcessor> processorMap = Maps.newHashMap();

    @SuppressWarnings("all")
    public static <T extends SettingProcessor> T getOrAdd(Class<T> processor) throws IllegalAccessException, InstantiationException {
        if (!processorMap.containsKey(processor)) {
            processorMap.put(processor, processor.newInstance());
        }

        return (T) processorMap.get(processor);
    }
}
