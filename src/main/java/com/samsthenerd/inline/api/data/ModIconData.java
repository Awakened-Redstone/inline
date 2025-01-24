package com.samsthenerd.inline.api.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.samsthenerd.inline.Inline;
import com.samsthenerd.inline.impl.InlineStyle;
import com.samsthenerd.inline.tooltips.CustomTooltipManager;
import com.samsthenerd.inline.tooltips.providers.ModDataTTProvider;
import com.samsthenerd.inline.utils.SpriteLike;
import com.samsthenerd.inline.utils.TextureSprite;
import com.samsthenerd.inline.utils.URLSprite;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Optional;

// mostly just extending so we can still use the renderer
public class ModIconData extends SpriteInlineData {
    public ModIconDataType getType() {
        return ModIconDataType.INSTANCE;
    }

    public static final SpriteLike MISSING_ICON = new TextureSprite(Inline.id("textures/missingicon.png"));

    public String modId;

    public ModIconData(String modId) {
        this(modId, true);
    }

    public ModIconData(String modId, boolean usePlaceholder) {
        super(spriteFromModId(modId, usePlaceholder));
        this.modId = modId;
    }

    @Nullable
    public static SpriteLike spriteFromModId(String modId, boolean usePlaceholder) {
        Optional<ModContainer> maybeMod = Inline.getMod(modId);
        if (maybeMod.isEmpty()) {
            return usePlaceholder ? MISSING_ICON : null;
        }
        ModContainer mod = maybeMod.get();
        try {
            Optional<String> logoFile = mod.getMetadata().getIconPath(128);
            if (logoFile.isEmpty()) return usePlaceholder ? MISSING_ICON : null;
            Optional<Path> logoPath = mod.findPath(logoFile.get());
            if (logoPath.isEmpty()) return usePlaceholder ? MISSING_ICON : null;
            return new URLSprite(logoPath.get().toUri().toURL().toString(), Identifier.of("inlinemodicon", mod.getMetadata().getId()));
        } catch (Exception e) {
            return usePlaceholder ? MISSING_ICON : null;
        }
    }

    public static Style getTooltipStyle(String modId) {
        Optional<ModContainer> maybeMod = Inline.getMod(modId);
        if (maybeMod.isEmpty()) {
            return Style.EMPTY;
        }
        ModContainer mod = maybeMod.get();

        HoverEvent he = new HoverEvent(
          HoverEvent.Action.SHOW_ITEM,
          new HoverEvent.ItemStackContent(CustomTooltipManager.getForTooltip(ModDataTTProvider.INSTANCE, mod))
        );
        Style styled = Style.EMPTY.withHoverEvent(he);
        Optional<String> homepageMaybe = mod.getMetadata().getContact().get("homepage");
        if (homepageMaybe.isPresent()) {
            ClickEvent ce = new ClickEvent(ClickEvent.Action.OPEN_URL, homepageMaybe.get().toString());
            styled = styled.withClickEvent(ce);
        }
        return styled;
    }

    public static Text makeModIcon(ModContainer mod) {
        Style dataStyle = InlineStyle.fromInlineData(new ModIconData(mod.getMetadata().getId()));
        return Text.literal(".").setStyle(dataStyle.withParent(getTooltipStyle(mod.getMetadata().getId())));
    }

    public static class ModIconDataType extends SpriteDataType {
        public static ModIconDataType INSTANCE = new ModIconDataType();

        @Override
        public Identifier getId() {
            return Inline.id("modicon");
        }

        @Override
        public Codec<SpriteInlineData> getCodec() {
            return Codec.STRING.flatComapMap(
              ModIconData::new,
              (SpriteInlineData data) -> {
                  if (!(data instanceof ModIconData mData)) {
                      return DataResult.error(() -> "");
                  }
                  return DataResult.success(mData.modId);
              }
            );
        }
    }
}
