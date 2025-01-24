package com.samsthenerd.inline.utils;

import com.mojang.authlib.GameProfile;
import com.samsthenerd.inline.mixin.feature.playerskins.MixinAccessPlayerModelParts;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class FakeClientPlayerMaker {
    public static Pair<Entity, Boolean> getPlayerEntity(ProfileComponent profile) {
        GameProfile betterProfile = getBetterProfile(profile);
        boolean isActuallyBetter = true;
        PlayerEntity player = new OtherClientPlayerEntity(MinecraftClient.getInstance().world, betterProfile) {
            @Override
            public boolean shouldRenderName() {
                return false;
            }
        };
        player.prevCapeY = player.capeY = (player.getY() - 0.5);
        player.prevCapeX = player.capeX = player.getX();
        player.prevCapeZ = player.capeZ = player.getZ();
        player.getDataTracker().set(MixinAccessPlayerModelParts.getPlayerModelParts(), (byte) 0b11111111);
        return new Pair<>(player, isActuallyBetter);
    }

    private static final Map<UUID, Optional<GameProfile>> UUID_PROFILE_CACHE = new HashMap<>();
    private static final Map<String, Optional<GameProfile>> NAME_PROFILE_CACHE = new HashMap<>();

    public static @NotNull GameProfile getBetterProfile(ProfileComponent profile) {
        // try to find the better profile in our caches
        if (profile.id().isPresent()) {
            Optional<GameProfile> maybeProf = UUID_PROFILE_CACHE.get(profile.id().get());
            if (maybeProf != null) {
                return maybeProf.orElse(profile.gameProfile());
            }
        }

        if (profile.name().isPresent()) {
            Optional<GameProfile> maybeProf = NAME_PROFILE_CACHE.get(profile.name().get().toLowerCase());
            if (maybeProf != null) {
                return maybeProf.orElse(profile.gameProfile());
            }
        }
        // can't find, try to fetch it

        // set these to empty optionals so we don't repeatedly fetch a ton
        if (profile.id().isPresent()) {
            UUID_PROFILE_CACHE.put(profile.id().get(), Optional.empty());
        }

        if (profile.name().isPresent()) {
            NAME_PROFILE_CACHE.put(profile.name().get().toLowerCase(), Optional.empty());
        }

        profile.getFuture().thenAcceptAsync(betterComponent -> {
            UUID_PROFILE_CACHE.put(betterComponent.gameProfile().getId(), Optional.of(betterComponent.gameProfile()));
            NAME_PROFILE_CACHE.put(betterComponent.gameProfile().getName().toLowerCase(), Optional.of(betterComponent.gameProfile()));
        });

        return profile.gameProfile();
    }
}
