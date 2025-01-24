package com.samsthenerd.inline.api.client;

import com.bawnorton.configurable.Configurable;
import com.bawnorton.configurable.Yacl;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;

public class InlineClientConfig {
    //@Configurable
    public static Set<Identifier> disabledMatchers = new HashSet<>();
    @Configurable
    public static boolean renderModIcon = true;
    /**
     * Sets if it should create display board mixins should apply.
     */
    @Configurable(yacl = @Yacl(customController = "com.samsthenerd.inline.api.client.compat.YACLCompat#requiresCreate"))
    public static boolean applyCreateMixins = true;
    /**
     * A value greater than 1 for how large an inline size modifier can be in chat.
     */
    @Configurable(min = 1)
    public static double chatScaleCap = 1.5;
}
