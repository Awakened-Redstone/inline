package com.samsthenerd.inline.api.data;

import com.mojang.serialization.Codec;
import com.samsthenerd.inline.Inline;
import com.samsthenerd.inline.api.InlineData;
import com.samsthenerd.inline.utils.SpriteLike;
import net.minecraft.util.Identifier;

public class SpriteInlineData implements InlineData<SpriteInlineData>{
    public SpriteDataType getType(){
        return SpriteDataType.INSTANCE;
    }

    @Override
    public Identifier getRendererId(){
        return Inline.id( "spritelike");
    }

    public final SpriteLike sprite;

    public SpriteInlineData(SpriteLike sprite){
        this.sprite = sprite;
    }

    public static class SpriteDataType implements InlineDataType<SpriteInlineData> {
        public static SpriteDataType INSTANCE = new SpriteDataType();

        @Override
        public Identifier getId(){
            return Inline.id( "spritelike");
        }

        @Override
        public Codec<SpriteInlineData> getCodec(){
            return SpriteLike.CODEC.xmap(
                SpriteInlineData::new,
                data -> data.sprite
            );
        }
    }
}
