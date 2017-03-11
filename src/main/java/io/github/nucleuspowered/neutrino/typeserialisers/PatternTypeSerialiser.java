/*
 * This file is part of Neutrino, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.neutrino.typeserialisers;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

import java.util.regex.Pattern;

public class PatternTypeSerialiser implements TypeSerializer<Pattern> {

    @Override public Pattern deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
        return Pattern.compile(value.getString());
    }

    @Override public void serialize(TypeToken<?> type, Pattern obj, ConfigurationNode value) throws ObjectMappingException {
        value.setValue(TypeToken.of(String.class), obj.pattern());
    }
}
