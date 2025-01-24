package com.samsthenerd.inline.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.samsthenerd.inline.api.InlineData;
import net.minecraft.text.Style;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * duck interface to carry added style data
 */
public interface InlineStyle {
    default InlineData getInlineData() {
        return getComponent(INLINE_DATA_COMP);
    }

    default Style withInlineData(InlineData data) {
        return null;
    }

    static Style fromInlineData(InlineData data) {
        return (Style.EMPTY).withInlineData(data);
    }

    default <C> C getComponent(InlineStyleComponent<C> component) {
        return null;
    }

    default Map<InlineStyleComponent<?>, Object> getComponentMap() {
        return null;
    }

    default Set<InlineStyleComponent<?>> getComponents() {
        return null;
    }

    default <C> Style withComponent(InlineStyleComponent<C> component, @Nullable C value) {
        return null;
    }

    default <C> Style setComponent(InlineStyleComponent<C> component, @Nullable C value) {
        return null;
    }

    // ensure that C has a valid .equals() in order for the styles to have it as well.
    record InlineStyleComponent<C>(String id, Codec<C> codec, C defaultValue, BiFunction<C, C, C> merger) {
        public static final Map<String, InlineStyleComponent<?>> ALL_COMPS = new HashMap<>();

        public static final Codec<InlineStyleComponent<?>> CODEC = Codec.STRING
          .comapFlatMap(
            id -> Optional.ofNullable(ALL_COMPS.get(id))
              .<DataResult<InlineStyleComponent<?>>>map(DataResult::success)
              .orElseGet(() -> DataResult.error(() -> "Unknown Inline style component: " + id)),
            InlineStyleComponent::id
          );

        public static final Codec<Map<InlineStyleComponent<?>, Object>> COMPONENT_TO_VALUE_MAP_CODEC = Codec.dispatchedMap(InlineStyle.InlineStyleComponent.CODEC, InlineStyle.InlineStyleComponent::codec);


        public InlineStyleComponent(String id, Codec<C> codec, C defaultValue, BiFunction<C, C, C> merger) {
            this.id = id;
            this.codec = codec;
            this.defaultValue = defaultValue;
            this.merger = merger;
            ALL_COMPS.put(id, this);
        }

        public InlineStyleComponent(String id, Codec<C> codec, C defaultValue) {
            this(id, codec, defaultValue, (a, b) -> a);
        }
    }

    InlineStyleComponent<InlineData<?>> INLINE_DATA_COMP = new InlineStyleComponent<>("inlinedata", InlineImpl.INLINE_DATA_CODEC, null);
    InlineStyleComponent<Boolean> HIDDEN_COMP = new InlineStyleComponent<>("hidden", Codec.BOOL, false);
    InlineStyleComponent<Double> SIZE_MODIFIER_COMP = new InlineStyleComponent<>("size", Codec.DOUBLE, 1.0);

    /**
     * GLOWY_MARKER_COMP indicates if the *currently rendered* text is an outline. ie, the outline is currently being
     * rendered and it has this style.
     */
    InlineStyleComponent<Boolean> GLOWY_MARKER_COMP = new InlineStyleComponent<>("glowy", Codec.BOOL, false);
    /**
     * GLOWY_PARENT_COMP indicates that this text *has* outlines, but that the currently rendered text is the center,
     * not the outline. It stores the color of the outline. This is really only used for if you want to self-handle the
     * glow outline.
     */
    InlineStyleComponent<Integer> GLOWY_PARENT_COMP = new InlineStyleComponent<>("glowyparent", Codec.INT, -1);

    static Style makeCopy(Style original) {
        return original.withColor(original.getColor());
    }
}
