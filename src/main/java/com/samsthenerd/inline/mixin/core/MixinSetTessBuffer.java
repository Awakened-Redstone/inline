package com.samsthenerd.inline.mixin.core;

import net.minecraft.client.render.Tessellator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Tessellator.class)
public interface MixinSetTessBuffer {
    @Accessor("INSTANCE") @Mutable static void setInstance(Tessellator tes){
        throw new AssertionError();
    }
}
