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
import io.github.nucleuspowered.neutrino.annotations.RequiresProperty;
import io.github.nucleuspowered.neutrino.settingprocessor.SettingProcessor;
import io.github.nucleuspowered.neutrino.settingprocessor.SettingProcessorCache;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.Setting;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

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
                data = new DefaultFieldData(field, comment, data, de.value(), de.saveDefaultIfNull(), de.useDefaultIfEmpty(), canEdit(field));
            } else if (!canEdit(field)) {
                data = new JavaPropertyFieldData(field, comment);
            }

            field.setAccessible(true);
            if (!fieldDataMapCache.containsKey(path)) {
                fieldDataMapCache.put(path, data);
            }
        }
    }

    private boolean canEdit(Field field) {
        if (!field.isAnnotationPresent(RequiresProperty.class)) {
            return true;
        }

        try {
            RequiresProperty annotation = field.getAnnotation(RequiresProperty.class);
            @Nullable String propertyValue = System.getProperty(annotation.value());
            return propertyValue != null && Pattern.compile(annotation.matchedName()).matcher(propertyValue).matches();
        } catch (Exception e) {
            Logger.getGlobal().warning("Field %s checks for property %s but the value regex \"%s\" is invalid. Not loading.");
            return false;
        }
    }

    protected static class DefaultFieldData extends FieldData {

        private final boolean useIfNullWhenSaving;
        private final String defaultValue;
        private final FieldData fieldData;
        private final TypeToken<?> typeToken;
        private final Field field;
        private final boolean useIfEmpty;
        private final boolean set;
        private final String comment;

        protected DefaultFieldData(Field field, String comment, FieldData data, String defaultValue, boolean useIfNullWhenSaving, boolean useIfEmpty, boolean set)
                throws ObjectMappingException {
            super(field, comment);
            this.comment = comment;
            this.field = field;
            this.typeToken = TypeToken.of(field.getGenericType());
            this.defaultValue = defaultValue;
            this.fieldData = data;
            this.useIfNullWhenSaving = useIfNullWhenSaving;
            this.useIfEmpty = useIfEmpty;
            this.set = set;
        }

        @Override public void deserializeFrom(Object instance, ConfigurationNode node) throws ObjectMappingException {
            if (!this.set) {
                try {
                    setDefaultOnField(instance, node);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

                return;
            }

            try {
                this.fieldData.deserializeFrom(instance, node);
            } catch (Exception e) {
                // ignored
            }

            try {
                if (node.isVirtual() || node.getValue() == null || (this.useIfEmpty && node.getString().isEmpty())) {
                    setDefaultOnField(instance, node);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        private void setDefaultOnField(Object instance, ConfigurationNode node) throws ObjectMappingException, IllegalAccessException {
            field.setAccessible(true);
            field.set(instance, node.getOptions().getSerializers().get(this.typeToken)
                    .deserialize(this.typeToken, SimpleConfigurationNode.root(node.getOptions()).setValue(this.defaultValue)));
        }

        @Override public void serializeTo(Object instance, ConfigurationNode node) throws ObjectMappingException {
            Object i;
            try {
                i = this.field.get(instance);
            } catch (IllegalAccessException e) {
                i = null;
            }

            if (this.set) {
                if (this.useIfNullWhenSaving && i == null) {
                    node.setValue(this.defaultValue);
                    if (this.comment != null && !this.comment.isEmpty() && node instanceof CommentedConfigurationNode) {
                        ((CommentedConfigurationNode) node).setComment(this.comment);
                    }
                } else {
                    this.fieldData.serializeTo(instance, node);
                }
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

    protected static class JavaPropertyFieldData extends FieldData {

        private static String COMMENT = "This config option is currently ignored.";

        public JavaPropertyFieldData(Field field, String comment) throws ObjectMappingException {
            super(field, comment);
        }

        @Override
        public void deserializeFrom(Object instance, ConfigurationNode node) throws ObjectMappingException {
            // Don't set the field
            // super.deserializeFrom(instance, node);
        }

        @Override
        public void serializeTo(Object instance, ConfigurationNode node) throws ObjectMappingException {
            if (!node.isVirtual() && node instanceof CommentedConfigurationNode) {
                CommentedConfigurationNode ccn = (CommentedConfigurationNode) node;
                String comment = ccn.getComment().orElse("");
                if (!comment.endsWith(COMMENT)) {
                    ccn.setComment(ccn.getComment() + System.lineSeparator() + COMMENT);
                }
            }
            // super.serializeTo(instance, node);
        }
    }

    protected static class PreprocessedFieldData extends FieldData {

        private final List<SettingProcessor> processors = new ArrayList<>();

        protected PreprocessedFieldData(Field field, String comment) throws ObjectMappingException, IllegalArgumentException {
            super(field, comment);
            try {
                for (Class<? extends SettingProcessor> pro : field.getAnnotation(ProcessSetting.class).value()) {
                    processors.add(SettingProcessorCache.getOrAdd(pro));
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
