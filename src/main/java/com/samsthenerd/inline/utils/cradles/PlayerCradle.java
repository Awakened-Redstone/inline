package com.samsthenerd.inline.utils.cradles;

import com.mojang.serialization.Codec;
import com.samsthenerd.inline.Inline;
import com.samsthenerd.inline.utils.EntityCradle;
import com.samsthenerd.inline.utils.FakeClientPlayerMaker;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.Util;
import net.minecraft.world.World;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.UUID;

/**
 * An entity cradle backed by a player ProfileComponent
 */
public class PlayerCradle extends EntityCradle {
    private static final HashMap<UUID, Entity> UUID_PLAYER_CACHE = new HashMap<>();
    private static final HashMap<String, Entity> NAME_PLAYER_CACHE = new HashMap<>();

    private final ProfileComponent profile;

    public PlayerCradle(ProfileComponent profile) {
        this.profile = profile;
    }

    public ProfileComponent getProfile() {
        return profile;
    }

    public CradleType<?> getType() {
        return PlayerCradleType.INSTANCE;
    }

    @Override
    public String getId() {
        return profile.id().isPresent() ? profile.id().get().toString() : profile.name().get();
    }

    public Entity getEntity(World world) {
        UUID playerId = profile.id().orElse(Util.NIL_UUID);
        if (playerId != Util.NIL_UUID && UUID_PLAYER_CACHE.containsKey(playerId)) {
            return UUID_PLAYER_CACHE.get(playerId);
        }
        String playerName = profile.name().orElse("");
        if (StringUtils.isNotBlank(playerName) && NAME_PLAYER_CACHE.containsKey(playerName)) {
            return NAME_PLAYER_CACHE.get(playerName);
        }

        if (!world.isClient()) {
            return null;
        }

        Pair<Entity, Boolean> playerRes = FakeClientPlayerMaker.getPlayerEntity(profile);
        if (playerRes.getRight() && playerId != Util.NIL_UUID) {
            UUID_PLAYER_CACHE.put(playerId, playerRes.getLeft());
        }
        if (playerRes.getRight() && StringUtils.isNotBlank(playerName)) {
            NAME_PLAYER_CACHE.put(playerName, playerRes.getLeft());
        }
        return playerRes.getLeft();
    }

    private static class PlayerCradleType implements CradleType<PlayerCradle> {

        public static PlayerCradleType INSTANCE = EntityCradle.addCradleType(new PlayerCradleType());

        public Identifier getId() {
            return Inline.id("nbt");
        }

        public Codec<PlayerCradle> getCodec() {
            return ProfileComponent.CODEC.xmap(
              PlayerCradle::new,
              PlayerCradle::getProfile
            );
        }
    }
}
