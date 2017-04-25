/*
 * This file is part of Neutrino, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.neutrino.typeserialisers;

import com.google.common.primitives.Shorts;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

import java.util.List;

public class ShortArrayTypeSerialiser implements TypeSerializer<short[]> {

    private final TypeToken<Short> ttb = new TypeToken<Short>() {};
    private final TypeToken<List<Short>> ttlb = new TypeToken<List<Short>>() {};

    @Override public short[] deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
        List<Short> list = value.getList(ttb);
        return Shorts.toArray(list);
    }

    @Override public void serialize(TypeToken<?> type, short[] obj, ConfigurationNode value) throws ObjectMappingException {
        List<Short> bytes = Shorts.asList(obj);
        value.setValue(ttlb, bytes);
    }
}
