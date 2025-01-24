package com.samsthenerd.inline.api.client.compat;

import com.samsthenerd.inline.Inline;
import dev.isxander.yacl3.api.Controller;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;

public class YACLCompat {
    public static Controller<Boolean> requiresCreate(Option<Boolean> option) {
        option.setAvailable(Inline.getMod("create").isPresent());
        return TickBoxControllerBuilder.create(option).build();
    }
}
