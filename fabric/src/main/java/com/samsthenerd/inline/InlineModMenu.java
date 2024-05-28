package com.samsthenerd.inline;

import com.samsthenerd.inline.config.InlineConfig;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class InlineModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> InlineConfig.getConfigScreen(parent);
    }
}
