/*
 * This file is part of Neutrino, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.neutrino.tests.configurate;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.neutrino.typeserialisers.SetTypeSerialiser;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Set;

public class TypeSerialiserTests {

    private TestConfigurationLoader getSetTestLoader() {
        TestConfigurationLoader.Builder tclb = TestConfigurationLoader.builder();
        TypeSerializerCollection tsc = tclb.getDefaultOptions().getSerializers();
        tsc.registerPredicate(
                typeToken -> Set.class.isAssignableFrom(typeToken.getRawType()),
                new SetTypeSerialiser()
        );

        tclb.setDefaultOptions(tclb.getDefaultOptions().setSerializers(tsc));
        return tclb.build();
    }

    @Test
    public void testThatSetsCanBeSerialised() throws ObjectMappingException {
        TestConfigurationLoader tcl = getSetTestLoader();
        ConfigurationNode cn = tcl.createEmptyNode().setValue(new TypeToken<Set<String>>() {}, Sets.newHashSet("test", "test2"));

        List<String> ls = cn.getList(TypeToken.of(String.class));
        Assert.assertTrue(ls.contains("test"));
        Assert.assertTrue(ls.contains("test2"));
    }

    @Test
    public void testThatSetsCanBeDeserialised() throws ObjectMappingException {
        TestConfigurationLoader tcl = getSetTestLoader();
        ConfigurationNode cn = tcl.createEmptyNode().setValue(new TypeToken<List<String>>() {}, Lists.newArrayList("test", "test", "test2"));

        Set<String> ls = cn.getValue(new TypeToken<Set<String>>() {});
        Assert.assertEquals(2, ls.size());
        Assert.assertTrue(ls.contains("test"));
        Assert.assertTrue(ls.contains("test2"));
    }
}
