/*
 * This file is part of Neutrino, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.neutrino.settingprocessor;

import com.google.common.collect.Maps;
import io.github.nucleuspowered.neutrino.util.ClassConstructor;

import java.util.Map;

public class SettingProcessorCache {

    private SettingProcessorCache() {}

    private static Map<Class<? extends SettingProcessor>, SettingProcessor> processorMap = Maps.newHashMap();

    @SuppressWarnings("all")
    public static <T extends SettingProcessor> T getOrAdd(Class<T> processor,
            ClassConstructor<SettingProcessor> constructor) throws Throwable {
        if (!processorMap.containsKey(processor)) {
            processorMap.put(processor, constructor.construct(processor));
        }

        return (T) processorMap.get(processor);
    }
}
