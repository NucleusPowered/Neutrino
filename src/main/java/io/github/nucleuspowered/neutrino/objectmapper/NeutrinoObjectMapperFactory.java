/*
 * This file is part of Neutrino, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.neutrino.objectmapper;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.github.nucleuspowered.neutrino.settingprocessor.SettingProcessor;
import io.github.nucleuspowered.neutrino.util.ClassConstructor;
import ninja.leaping.configurate.objectmapping.ObjectMapperFactory;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.Setting;

import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NeutrinoObjectMapperFactory implements ObjectMapperFactory {

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builds an {@link NeutrinoObjectMapperFactory}
     */
    public static class Builder {

        @Nullable private Function<Setting, String> commentProcessor;
        private ClassConstructor<SettingProcessor> settingProcessorClassConstructor = Class::newInstance;

        public Builder setCommentProcessor(@Nullable Function<Setting, String> commentProcessor) {
            this.commentProcessor = commentProcessor;
            return this;
        }

        public Builder setSettingProcessorClassConstructor(ClassConstructor<SettingProcessor> settingProcessorClassConstructor) {
            this.settingProcessorClassConstructor = Preconditions.checkNotNull(settingProcessorClassConstructor);
            return this;
        }

        public NeutrinoObjectMapperFactory build(boolean setAsDefault) {
            if (commentProcessor == null) {
                this.commentProcessor = Setting::comment;
            }

            return new NeutrinoObjectMapperFactory(setAsDefault, this.commentProcessor, this.settingProcessorClassConstructor);
        }

    }
    private static NeutrinoObjectMapperFactory INSTANCE;

    @Deprecated
    public static ObjectMapperFactory getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NeutrinoObjectMapperFactory(false, Setting::comment, Class::newInstance);
        }

        return INSTANCE;
    }

    // --

    private final ClassConstructor<SettingProcessor> processorClassConstructor;
    private final Function<Setting, String> commentProcessor;
    private final LoadingCache<Class<?>, NeutrinoObjectMapper<?>> mapperCache = CacheBuilder.newBuilder().weakKeys()
            .maximumSize(500).build(new CacheLoader<Class<?>, NeutrinoObjectMapper<?>>() {
                @Override
                public NeutrinoObjectMapper<?> load(@Nonnull Class<?> key) throws Exception {
                    return new NeutrinoObjectMapper<>(key, commentProcessor, processorClassConstructor);
                }
            });

    private NeutrinoObjectMapperFactory(boolean setInstance, Function<Setting, String> commentProcessor, ClassConstructor<SettingProcessor> processorClassConstructor) {
        this.commentProcessor = commentProcessor;
        this.processorClassConstructor = processorClassConstructor;
        if (INSTANCE == null || setInstance) {
            INSTANCE = this;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> NeutrinoObjectMapper<T> getMapper(Class<T> type) throws ObjectMappingException {
        Preconditions.checkNotNull(type, "type");
        try {
            return (NeutrinoObjectMapper<T>) mapperCache.get(type);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof ObjectMappingException) {
                throw (ObjectMappingException) e.getCause();
            } else {
                throw new ObjectMappingException(e);
            }
        }
    }

}
