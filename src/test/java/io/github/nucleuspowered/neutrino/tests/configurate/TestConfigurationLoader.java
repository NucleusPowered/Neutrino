/*
 * This file is part of Neutrino, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.neutrino.tests.configurate;

import io.github.nucleuspowered.neutrino.objectmapper.NeutrinoObjectMapperFactory;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.loader.AbstractConfigurationLoader;
import ninja.leaping.configurate.loader.CommentHandlers;
import org.mockito.Mockito;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Largely taken from https://github.com/zml2008/configurate/blob/master/configurate-core/src/test/java/ninja/leaping/configurate/loader/TestConfigurationLoader.java
 */
public class TestConfigurationLoader extends AbstractConfigurationLoader<ConfigurationNode> {

    private ConfigurationNode result = SimpleConfigurationNode.root();

    public static final class Builder extends AbstractConfigurationLoader.Builder<Builder> {

        @Override
        public TestConfigurationLoader build() {
            setDefaultOptions(getDefaultOptions().setObjectMapperFactory(NeutrinoObjectMapperFactory.getInstance()))
                    .setSource(() -> Mockito.mock(BufferedReader.class))
                    .setSink(() -> Mockito.mock(BufferedWriter.class));
            return new TestConfigurationLoader(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    protected TestConfigurationLoader(Builder builder) {
        super(builder, CommentHandlers.values());
    }

    @Override
    protected void loadInternal(ConfigurationNode node, BufferedReader reader) throws IOException {
        node.setValue(result);
    }

    @Override
    protected void saveInternal(ConfigurationNode node, Writer writer) throws IOException {
        result.setValue(node);
    }

    public ConfigurationNode getNode() {
        return this.result;
    }

    public void setNode(ConfigurationNode node) {
        this.result = node;
    }

    /**
     * Return an empty node of the most appropriate type for this loader
     *
     * @param options The options to use with this node. Must not be null (take a look at {@link ConfigurationOptions#defaults()})
     * @return The appropriate node type
     */
    @Override
    public ConfigurationNode createEmptyNode(ConfigurationOptions options) {
        return SimpleConfigurationNode.root(options);
    }
}
