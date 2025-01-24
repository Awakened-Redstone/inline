package com.samsthenerd.inline.mixin.feature.playerskins;

import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftClient.class)
public interface MixinClientAccessor {
    @Accessor("authenticationService")
    YggdrasilAuthenticationService getAuthenticationService();
}
