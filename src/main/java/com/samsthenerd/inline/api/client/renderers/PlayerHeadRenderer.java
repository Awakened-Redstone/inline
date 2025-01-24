package com.samsthenerd.inline.api.client.renderers;

import com.mojang.authlib.GameProfile;
import com.samsthenerd.inline.Inline;
import com.samsthenerd.inline.api.client.GlowHandling;
import com.samsthenerd.inline.api.client.InlineRenderer;
import com.samsthenerd.inline.api.data.PlayerHeadData;
import com.samsthenerd.inline.api.data.SpriteInlineData;
import com.samsthenerd.inline.utils.FakeClientPlayerMaker;
import com.samsthenerd.inline.utils.TextureSprite;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import net.minecraft.util.Identifier;

public class PlayerHeadRenderer implements InlineRenderer<PlayerHeadData> {

    public static final PlayerHeadRenderer INSTANCE = new PlayerHeadRenderer();

    @Override
    public Identifier getId() {
        return Inline.id("playerhead");
    }

    public Identifier textureFromHeadData(PlayerHeadData data) {
        GameProfile prof = FakeClientPlayerMaker.getBetterProfile(data.profile());
        return MinecraftClient.getInstance().getSkinProvider().getSkinTextures(prof).texture();
    }

    public SpriteInlineData getFace(PlayerHeadData data) {
        TextureSprite faceSprite = TextureSprite.fromPixels(textureFromHeadData(data),
          8, 8, 8, 8, 64, 64);
        return new SpriteInlineData(faceSprite);
    }

    public SpriteInlineData getOuter(PlayerHeadData data) {
        TextureSprite faceSprite = TextureSprite.fromPixels(textureFromHeadData(data),
          40, 8, 8, 8, 64, 64);
        return new SpriteInlineData(faceSprite);
    }

    @Override
    public int render(PlayerHeadData data, DrawContext context, int index, Style style, int codepoint, TextRenderingContext trContext) {
        SpriteInlineData faceSpriteData = getFace(data);
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(0.25, 0.25, 0);
        matrices.scale(8 / (float) 8.5, 8 / (float) 8.5, 1);
        InlineSpriteRenderer.INSTANCE.render(faceSpriteData, context, index, style, codepoint, trContext);
        matrices.pop();
        SpriteInlineData outerSpriteData = getOuter(data);
        matrices.push();
        matrices.translate(0, 0, 10);
        int res = InlineSpriteRenderer.INSTANCE.render(outerSpriteData, context, index, style, codepoint, trContext);
        matrices.pop();
        return res;
    }

    @Override
    public int charWidth(PlayerHeadData data, Style style, int codepoint) {
        return 8;
    }

    @Override
    public GlowHandling getGlowPreference(PlayerHeadData forData) {
        // silly but should be fine.
        if (forData.profile().name().isPresent()) {
            return new GlowHandling.Full(forData.profile().name().get().toLowerCase());
        }
        if (forData.profile().id().isPresent()) {
            return new GlowHandling.Full(forData.profile().id().get().toString());
        }
        return new GlowHandling.Full();
    }
}
