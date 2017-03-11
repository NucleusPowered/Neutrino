/*
 * This file is part of Neutrino, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.neutrino.objectmapper;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.neutrino.annotations.Default;
import io.github.nucleuspowered.neutrino.annotations.DoNotGenerate;
import io.github.nucleuspowered.neutrino.annotations.ProcessSetting;
import io.github.nucleuspowered.neutrino.settingprocessor.SettingProcessor;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.Setting;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class NeutrinoObjectMapper<T> extends ObjectMapper<T> {

    private final Function<Setting, String> commentProcessor;
    private Map<String, FieldData> fieldDataMapCache;
    private List<Field> fieldsToProcess;

    /**
     * Create a new object mapper of a given type
     *
     * @param clazz The type this object mapper will work with
     * @throws ObjectMappingException if the provided class is in someway invalid
     */
    NeutrinoObjectMapper(Class<T> clazz, Function<Setting, String> commentProcessor) throws ObjectMappingException {
        super(clazz);
        this.commentProcessor = commentProcessor;
        collectFields();
    }

    // Come back and do our processing later.
    protected void collectFields(Map<String, FieldData> cachedFields, Class<? super T> clazz) throws ObjectMappingException {
        if (this.fieldDataMapCache == null) {
            this.fieldDataMapCache = cachedFields;
            this.fieldsToProcess = Lists.newArrayList();
        }

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Setting.class)) {
                fieldsToProcess.add(field);
            }
        }
    }

    protected void collectFields() throws ObjectMappingException {
        for (Field field : fieldsToProcess) {
            Setting setting = field.getAnnotation(Setting.class);
            String path = setting.value();
            if (path.isEmpty()) {
                path = field.getName();
            }

            String comment = commentProcessor.apply(setting);

            FieldData data;
            if (field.isAnnotationPresent(ProcessSetting.class)) {
                try {
                    data = new PreprocessedFieldData(field, comment);
                } catch (IllegalArgumentException e) {
                    data = new FieldData(field, comment);
                }
            } else if (field.isAnnotationPresent(DoNotGenerate.class)) {
                Object defaultValue = null;
                try {
                    field.setAccessible(true);
                    defaultValue = field.get(field.getDeclaringClass().newInstance());
                } catch (IllegalAccessException | InstantiationException e) {
                    e.printStackTrace();
                }

                data = new DoNotGenerateFieldData(field, comment, defaultValue);
            } else {
                data = new FieldData(field, comment);
            }

            if (field.isAnnotationPresent(Default.class)) {
                Default de = field.getAnnotation(Default.class);
                data = new DefaultFieldData(field, comment, data, de.value(), de.saveDefaultIfNull(), de.useDefaultIfEmpty());
            }

            field.setAccessible(true);
            if (!fieldDataMapCache.containsKey(path)) {
                fieldDataMapCache.put(path, data);
            }
        }
    }

    protected static class DefaultFieldData extends FieldData {

        private final boolean useIfNullWhenSaving;
        private final String defaultValue;
        private final FieldData fieldData;
        private final TypeToken<?> typeToken;
        private final Field field;
        private final boolean useIfEmpty;

        protected DefaultFieldData(Field field, String comment, FieldData data, String defaultValue, boolean useIfNullWhenSaving, boolean useIfEmpty)
                throws ObjectMappingException {
            super(field, comment);
            this.field = field;
            this.typeToken = TypeToken.of(field.getGenericType());
            this.defaultValue = defaultValue;
            this.fieldData = data;
            this.useIfNullWhenSaving = useIfNullWhenSaving;
            this.useIfEmpty = useIfEmpty;
        }

        @Override public void deserializeFrom(Object instance, ConfigurationNode node) throws ObjectMappingException {
            try {
                this.fieldData.deserializeFrom(instance, node);
            } catch (Exception e) {
                // ignored
            }

            try {
                if (node.isVirtual() || node.getValue() == null || (this.useIfEmpty && node.getString().isEmpty())) {
                    field.setAccessible(true);
                    field.set(instance, node.getOptions().getSerializers().get(this.typeToken)
                        .deserialize(this.typeToken, SimpleConfigurationNode.root().setValue(this.defaultValue)));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        @Override public void serializeTo(Object instance, ConfigurationNode node) throws ObjectMappingException {
            if (this.useIfNullWhenSaving && instance == null) {
                node.setValue(this.defaultValue);
            } else {
                this.fieldData.serializeTo(instance, node);
            }
        }
    }

    protected static class DoNotGenerateFieldData extends FieldData {

        private final Object defaultValue;
        private final Field field;

        protected DoNotGenerateFieldData(Field field, String comment, Object defaultValue) throws ObjectMappingException {
            super(field, comment);
            this.field = field;
            this.defaultValue = defaultValue;
        }

        @Override
        public void serializeTo(Object instance, ConfigurationNode node) throws ObjectMappingException {
            try {
                field.setAccessible(true);
                if (!defaultValue.equals(field.get(instance))) {
                    super.serializeTo(instance, node);
                }
            } catch (IllegalAccessException e) {
                super.serializeTo(instance, node);
            }
        }
    }

    protected static class PreprocessedFieldData extends FieldData {

        private final List<SettingProcessor> processors = new ArrayList<>();

        protected PreprocessedFieldData(Field field, String comment) throws ObjectMappingException, IllegalArgumentException {
            super(field, comment);
            try {
                for (Class<? extends SettingProcessor> pro : field.getAnnotation(ProcessSetting.class).value()) {
                    processors.add(pro.newInstance());
                }
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
                throw new IllegalArgumentException("No setting processor", e);
            }
        }

        @Override
        public void deserializeFrom(Object instance, ConfigurationNode node) throws ObjectMappingException {
            for (SettingProcessor processor : processors) {
                processor.onGet(node);
            }

            super.deserializeFrom(instance, node);
        }

        @Override
        public void serializeTo(Object instance, ConfigurationNode node) throws ObjectMappingException {
            super.serializeTo(instance, node);

            for (SettingProcessor processor : processors) {
                processor.onSet(node);
            }
        }
    }
}
