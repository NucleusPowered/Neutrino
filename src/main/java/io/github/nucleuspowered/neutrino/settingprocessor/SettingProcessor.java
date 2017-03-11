/*
 * This file is part of Neutrino, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.neutrino.settingprocessor;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

/**
 * Setting Processors, when used in conjunction on a {@link ninja.leaping.configurate.objectmapping.Setting} annotated
 * field within a {@link ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable} class, allow for some
 * lightweight processing of getting and setting the node value.
 *
 * <p>
 *     Implementors must ensure that there is a parameterless constructor.
 * </p>
 *
 * <p>
 *     While this is a {@link FunctionalInterface} for simpler matters, the two default methods can be overridden if
 *     required.
 * </p>
 */
@FunctionalInterface
public interface SettingProcessor {

    /**
     * Transforms the node before it's set on the {@link ninja.leaping.configurate.objectmapping.Setting}.
     *
     * @param input The input {@link ConfigurationNode}
     * @throws ObjectMappingException thrown if the node cannot be loaded.
     */
    default void onGet(ConfigurationNode input) throws ObjectMappingException {
        process(input);
    }

    /**
     * Transforms the node before it's set in the configuration file.
     *
     * @param output The output {@link ConfigurationNode}
     * @throws ObjectMappingException thrown if the node cannot be loaded.
     */
    default void onSet(ConfigurationNode output) throws ObjectMappingException {
        process(output);
    }

    /**
     * By default, processes the configuration node and transforms it into the requested form.
     *
     * @param cn The {@link ConfigurationNode}
     * @throws ObjectMappingException thrown if the node cannot be transformed.
     */
    void process(ConfigurationNode cn) throws ObjectMappingException;
}
