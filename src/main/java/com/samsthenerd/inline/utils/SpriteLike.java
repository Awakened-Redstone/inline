package com.samsthenerd.inline.utils;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.samsthenerd.inline.utils.TextureSprite.TextureSpriteType;
import com.samsthenerd.inline.utils.URLSprite.UrlSpriteType;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * A wrapper around various texture sources. 
 * <p>
 * Spritelike is server safe, on the client it renders with a SpritelikeRenderer.
 * You shouldn't need to make new Spritelike types. 
 * 
 * @see URLSprite
 * @see TextureSprite
 */
public abstract class SpriteLike {

    public abstract SpritelikeType getType();

    public abstract Identifier getTextureId();

    public abstract float getMinU();
    public abstract float getMinV();
    public abstract float getMaxU();
    public abstract float getMaxV();

    // these are mostly just here for the w:h ratio
    public abstract int getTextureWidth();
    public abstract int getTextureHeight();

    public int getSpriteWidth(){
        return (int) ((getMaxU()-getMinU()) * getTextureWidth());
    }

    public int getSpriteHeight(){
        return (int) ((getMaxV()-getMinV()) * getTextureHeight());
    }

    

    public static SpriteLike fromJson(JsonElement json){
        return SpriteLike.CODEC.parse(JsonOps.INSTANCE, (json))
            .resultOrPartial(error -> {})
	        .orElse(null);
    }

    public static SpriteLike fromNbt(NbtElement nbt){
        return SpriteLike.CODEC.parse(NbtOps.INSTANCE, (nbt))
            .resultOrPartial(error -> {})
	        .orElse(null);
    }

    private static final Map<String, SpritelikeType> TYPES = new HashMap<>();

    static{
        registerType(UrlSpriteType.INSTANCE);
        registerType(TextureSpriteType.INSTANCE);
    }

    public static void registerType(SpritelikeType type){
        TYPES.put(type.getId(), type);
    }

    private static final Codec<SpritelikeType> TYPE_CODEC = Codec.STRING.comapFlatMap(id -> {
        SpritelikeType type = TYPES.get(id);
        if(type == null){
            return DataResult.error(()->{return "Unknown spritelike type: " + id;});
        }
        return DataResult.success(type);
    }, SpritelikeType::getId);

    public static final Codec<SpriteLike> CODEC = TYPE_CODEC.dispatch("type", SpriteLike::getType, SpritelikeType::getCodec);

    public interface SpritelikeType{
        public MapCodec<? extends SpriteLike> getCodec();

        public String getId();

        public static SpritelikeType of(String id, MapCodec<SpriteLike> codec){
            return new Simple(id, codec);
        }

        public static class Simple implements SpritelikeType{
            private final String id;
            private final MapCodec<SpriteLike> codec;

            public Simple(String id, MapCodec<SpriteLike> codec){
                this.id = id;
                this.codec = codec;
            }

            @Override
            public MapCodec<SpriteLike> getCodec(){
                return codec;
            }

            @Override
            public String getId(){
                return id;
            }
        }
    }


}
