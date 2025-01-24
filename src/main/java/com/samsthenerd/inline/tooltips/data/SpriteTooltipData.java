package com.samsthenerd.inline.tooltips.data;

import com.samsthenerd.inline.utils.SpriteLike;
import net.minecraft.item.tooltip.TooltipData;

import java.util.function.BiFunction;

// maxWidth/maxHeight are for how big it should render
public class SpriteTooltipData implements TooltipData {
    public final SpriteLike sprite;
    // takes in the  width and height of the given texture and returns the width to render it at
    public BiFunction<Integer, Integer, Integer> widthProvider = (w, h) -> 128;

    public SpriteTooltipData(SpriteLike sprite){
        this.sprite = sprite;
    }

    public SpriteTooltipData(SpriteLike sprite, BiFunction<Integer, Integer, Integer> widthProvider){
        this.sprite = sprite;
        this.widthProvider = widthProvider;
    }
}
