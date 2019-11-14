/*
 * This file is part of Neutrino, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.neutrino.util;

public interface ClassConstructor<S> {

    <T extends S> T construct(Class<T> clazz) throws Throwable;

}
