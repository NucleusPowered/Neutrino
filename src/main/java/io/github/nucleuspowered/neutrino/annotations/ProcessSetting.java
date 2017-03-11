/*
 * This file is part of Neutrino, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.neutrino.annotations;

import io.github.nucleuspowered.neutrino.settingprocessor.SettingProcessor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that, when this annotation is present alongside a {@link ninja.leaping.configurate.objectmapping.Setting}
 * annotation, some transformation of the node should occur at the following moments:
 *
 * <ul>
 *     <li>before the value of the field is set from the {@link ninja.leaping.configurate.ConfigurationNode}</li>
 *     <li>after the value of the field is read from the {@link ninja.leaping.configurate.ConfigurationNode}, but just
 *     before it is written to the file</li>
 * </ul>
 *
 * <p>
 *     {@link SettingProcessor}s are used to take the {@link ninja.leaping.configurate.ConfigurationNode}s and alter
 *     them directly before the final value for the task is read.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface ProcessSetting {

    /**
     * The {@link Class}es that represent the {@link SettingProcessor}s to run on the
     * {@link ninja.leaping.configurate.ConfigurationNode} that populates this field. The processors are executed in
     * the order provided here. They must all have parameterless constructors.
     *
     * @return The {@link Class}es of the {@link SettingProcessor}s to use.
     */
    Class<? extends SettingProcessor>[] value();
}
