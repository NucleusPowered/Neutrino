/*
 * This file is part of Neutrino, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.neutrino.settingprocessor;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.util.List;
import java.util.stream.Collectors;

public class LowercaseListSettingProcessor implements SettingProcessor {

    private final TypeToken<List<String>> ttListString = new TypeToken<List<String>>(String.class) {};
    private final TypeToken<String> ttString = TypeToken.of(String.class);

    @Override public void process(ConfigurationNode cn) throws ObjectMappingException {
        if (cn.isVirtual()) {
            return;
        }

        cn.setValue(ttListString, cn.getList(ttString).stream().map(x -> x.toLowerCase().replace(" ", "_")).collect(Collectors.toList()));
    }
}
