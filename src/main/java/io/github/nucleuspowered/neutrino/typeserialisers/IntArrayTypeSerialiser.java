/*
 * This file is part of Neutrino, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.neutrino.typeserialisers;

import com.google.common.primitives.Ints;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

import java.util.List;

public class IntArrayTypeSerialiser implements TypeSerializer<int[]> {

    private final TypeToken<Integer> ttb = new TypeToken<Integer>() {};
    private final TypeToken<List<Integer>> ttlb = new TypeToken<List<Integer>>() {};

    @Override public int[] deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
        List<Integer> list = value.getList(ttb);
        return Ints.toArray(list);
    }

    @Override public void serialize(TypeToken<?> type, int[] obj, ConfigurationNode value) throws ObjectMappingException {
        List<Integer> bytes = Ints.asList(obj);
        value.setValue(ttlb, bytes);
    }
}
