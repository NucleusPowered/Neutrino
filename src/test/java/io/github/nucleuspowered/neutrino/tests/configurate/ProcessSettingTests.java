/*
 * This file is part of Neutrino, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.neutrino.tests.configurate;

import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.neutrino.annotations.ProcessSetting;
import io.github.nucleuspowered.neutrino.settingprocessor.SettingProcessor;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class ProcessSettingTests {

    @Test
    public void testOnGet() throws ObjectMappingException, IOException {
        ConfigurationNode cn = TestConfigurationLoader.builder().build().load();
        cn.getNode("test").setValue("test");
        cn.getNode("test2").setValue("test");

        TestConfig tc = cn.getValue(TypeToken.of(TestConfig.class));

        Assert.assertEquals("transformed", tc.getTest());
        Assert.assertEquals("test", tc.getTest2());
    }

    @Test
    public void testOnSet() throws ObjectMappingException, IOException {
        TestConfig tc = new TestConfig();
        tc.test = "test";
        tc.test2 = "test";

        TestConfigurationLoader tcl = TestConfigurationLoader.builder().build();
        ConfigurationNode cn = tcl.createEmptyNode(tcl.getDefaultOptions()).setValue(TypeToken.of(TestConfig.class), tc);

        Assert.assertEquals("transformed", cn.getNode("test").getString());
        Assert.assertEquals("test", cn.getNode("test2").getString());
    }

    @ConfigSerializable
    public static class TestConfig {

        @Setting
        @ProcessSetting(TestProcess.class)
        private String test;

        @Setting
        private String test2;

        public String getTest() {
            return test;
        }

        public String getTest2() {
            return test2;
        }
    }

    public static class TestProcess implements SettingProcessor {

        @Override
        public void process(ConfigurationNode cn) throws ObjectMappingException {
            cn.setValue("transformed");
        }
    }
}
