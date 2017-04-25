/*
 * This file is part of Neutrino, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.neutrino.typeserialisers;

import com.google.common.primitives.Bytes;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

import java.util.List;

public class ByteArrayTypeSerialiser implements TypeSerializer<byte[]> {

    private final TypeToken<Byte> ttb = new TypeToken<Byte>() {};
    private final TypeToken<List<Byte>> ttlb = new TypeToken<List<Byte>>() {};

    @Override public byte[] deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
        List<Byte> list = value.getList(ttb);
        return Bytes.toArray(list);
    }

    @Override public void serialize(TypeToken<?> type, byte[] obj, ConfigurationNode value) throws ObjectMappingException {
        List<Byte> bytes = Bytes.asList(obj);
        value.setValue(ttlb, bytes);
    }
}
