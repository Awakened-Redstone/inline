package com.samsthenerd.inline.tooltips.providers;

import com.mojang.serialization.Codec;
import com.samsthenerd.inline.Inline;
import com.samsthenerd.inline.tooltips.CustomTooltipManager.CustomTooltipProvider;
import com.samsthenerd.inline.tooltips.data.EntityDisplayTTData;
import com.samsthenerd.inline.utils.EntityCradle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EntityTTProvider implements CustomTooltipProvider<EntityCradle> {
    public static final EntityTTProvider INSTANCE = new EntityTTProvider();

    @Override
    public Identifier getId(){
        return Inline.id("entitydisplay");
    }

    @Override
    @NotNull
    public List<Text> getTooltipText(EntityCradle cradle){
        List<Text> ttText = new ArrayList<>();
        Entity ent = cradle.getEntity(MinecraftClient.getInstance().world);
        if(ent != null) ttText.add(ent.getName());
        return ttText;
    }

    @Override
    @NotNull
    public Optional<TooltipData> getTooltipData(EntityCradle cradle){
        return Optional.of(new EntityDisplayTTData(cradle, (w,h) -> h == 0 ? 0 : Math.min(w * 96 /h, 96) ));
    }

    @Override
    public Codec<EntityCradle> getCodec(){
        return EntityCradle.CRADLE_CODEC;
    }
}
