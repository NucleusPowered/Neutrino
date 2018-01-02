/*
 * This file is part of Neutrino, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.neutrino.tests.configurate;

import io.github.nucleuspowered.neutrino.annotations.Default;
import io.github.nucleuspowered.neutrino.annotations.RequiresProperty;
import io.github.nucleuspowered.neutrino.objectmapper.NeutrinoObjectMapperFactory;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.junit.Assert;
import org.junit.Test;

public class RequiresPropertyTests {

    @Test
    public void testTestIsNotAltered() throws Exception {
        System.setProperty("neutrino.test2", "true");
        CommentedConfigurationNode ccn = SimpleCommentedConfigurationNode.root();
        ccn.getNode("test").setValue("ok");
        ccn.getNode("def").setValue("ok");
        ccn.getNode("updated").setValue("ok");

        TestConf sut = NeutrinoObjectMapperFactory.getInstance().getMapper(TestConf.class).bindToNew().populate(ccn);
        Assert.assertEquals("not", sut.test);
        Assert.assertEquals("def", sut.def);
        Assert.assertEquals("ok", sut.updated);
    }

    @Test
    public void testNodeIsNotAltered() throws Exception {
        System.setProperty("neutrino.test2", "true");
        CommentedConfigurationNode ccn = SimpleCommentedConfigurationNode.root();
        ccn.getNode("test").setValue("ok");
        ccn.getNode("def").setValue("ok");
        ccn.getNode("updated").setValue("ok");

        TestConf sut = new TestConf();
        NeutrinoObjectMapperFactory.getInstance().getMapper(TestConf.class).bind(sut).serialize(ccn);
        Assert.assertEquals("ok", ccn.getNode("test").getString());
        Assert.assertEquals("ok", ccn.getNode("def").getString());
        Assert.assertEquals("not", ccn.getNode("updated").getString());
    }

    @ConfigSerializable
    public static class TestConf {

        @RequiresProperty("neutrino.test")
        @Setting("test")
        private String test = "not";

        @RequiresProperty("neutrino.test")
        @Default("def")
        @Setting("def")
        private String def = "not";

        @RequiresProperty("neutrino.test2")
        @Setting("updated")
        private String updated = "not";
    }
}
