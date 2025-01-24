package com.samsthenerd.inline.tooltips.providers;

import com.mojang.serialization.Codec;
import com.samsthenerd.inline.Inline;
import com.samsthenerd.inline.api.data.ModIconData;
import com.samsthenerd.inline.tooltips.CustomTooltipManager.CustomTooltipProvider;
import com.samsthenerd.inline.tooltips.data.SpriteTooltipData;
import com.samsthenerd.inline.utils.SpriteLike;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ModDataTTProvider implements CustomTooltipProvider<ModContainer> {

    public static final ModDataTTProvider INSTANCE = new ModDataTTProvider();

    @Override
    public Identifier getId() {
        return Inline.id("moddata");
    }

    @Override
    @NotNull
    public List<Text> getTooltipText(ModContainer mod) {
        List<Text> modInfo = new ArrayList<>();
        if (mod == null) return modInfo;
        MutableText title = Text.literal(mod.getMetadata().getName()).setStyle(Style.EMPTY.withBold(true));
        MutableText description = Text.literal(mod.getMetadata().getDescription().replace("\n", "")).setStyle(Style.EMPTY.withColor(Formatting.GRAY));
        modInfo.add(title);
        modInfo.add(description);
        return modInfo;
    }

    @Override
    @NotNull
    public Optional<TooltipData> getTooltipData(ModContainer mod) {
        if (mod == null) return Optional.empty();

        SpriteLike iconSprite = ModIconData.spriteFromModId(mod.getMetadata().getId(), false);
        if (iconSprite == null) return Optional.empty();
        return Optional.of(new SpriteTooltipData(iconSprite, (w, h) -> 32));
    }

    @Override
    @NotNull
    public Codec<ModContainer> getCodec() {
        return Codec.STRING.xmap(
          modId -> Inline.getMod(modId).orElse(null),
          container -> container.getMetadata().getId()
        );
    }
}
