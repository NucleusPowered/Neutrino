/*
 * This file is part of Neutrino, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.neutrino.typeserialisers;

import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SetTypeSerialiser implements TypeSerializer<Set<?>> {

    @Override
    public Set<?> deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
        return new HashSet<>(value.getList(getInnerToken(type)));
    }

    @Override
    public void serialize(TypeToken<?> type, Set<?> obj, ConfigurationNode value) throws ObjectMappingException {
        value.setValue(getListTokenFromSet(type), new ArrayList<>(obj));
    }

    private TypeToken<?> getInnerToken(TypeToken<?> type) {
        return type.resolveType(Set.class.getTypeParameters()[0]);
    }

    @SuppressWarnings("unchecked")
    private <E> TypeToken<List<E>> getListTokenFromSet(TypeToken<?> type) {
        // Get the inner type out of the type token
        TypeToken<?> innerType = getInnerToken(type);

        // Put it into the new list token
        return new TypeToken<List<E>>() {}.where(new TypeParameter<E>() {}, (TypeToken<E>)innerType);
    }
}
