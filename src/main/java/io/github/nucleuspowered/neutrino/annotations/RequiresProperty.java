/*
 * This file is part of Neutrino, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.neutrino.annotations;

import ninja.leaping.configurate.objectmapping.Setting;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * For any {@link Setting} decorated with this, it will only
 * be updated if the Java property named in {@link #value()}
 * is set.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface RequiresProperty {

    String value();
}
