package com.samsthenerd.inline.mixin.core;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.samsthenerd.inline.impl.InlineStyle;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.function.Function;

@Mixin(Style.Codecs.class)
public class MixinInlineStyleSerializer {
    @Shadow @Final @Mutable public static MapCodec<Style> MAP_CODEC;

    @Unique
    private static MapCodec<Style> INLINE_CODEC;

    @Inject(method = "<clinit>", at = @At(value = "INVOKE_ASSIGN", target = "Lcom/mojang/serialization/MapCodec;codec()Lcom/mojang/serialization/Codec;", ordinal = 0, shift = At.Shift.BEFORE))
    private static void extendCodec(CallbackInfo ci) {
        INLINE_CODEC = RecordCodecBuilder.mapCodec(instance ->
          instance.group(
            // Use the last MAP_CODEC, keeping any modified one from other mods
            RecordCodecBuilder.of(Function.identity(), MAP_CODEC),
            // Add extra stuff to parse, with it being optional
            InlineStyle.InlineStyleComponent.COMPONENT_TO_VALUE_MAP_CODEC.optionalFieldOf("inline_components", Map.of()).forGetter(InlineStyle::getComponentMap)
          ).apply(instance, (original, components) -> {
              components.forEach((component, value) -> {
                  original.setComponent((InlineStyle.InlineStyleComponent<? super Object>) component, value);
              });
              return original;
          })
        );

        MAP_CODEC = INLINE_CODEC;
    }

    /*//TODO: FIX
    @Unique
    private static final String COMP_KEY = "inlinecomps";

    //@ModifyReturnValue(method = "deserialize", at = @At("RETURN"))
    private Style InlineStyDeserialize(Style initialStyle, JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
        if (!jsonElement.isJsonObject() || initialStyle == null) {
            return initialStyle;
        }
        JsonObject json = jsonElement.getAsJsonObject();
        if (!json.has(COMP_KEY)) {
            return initialStyle;
        }
        Style copiedStyle = InlineStyle.makeCopy(initialStyle);
        for (Map.Entry<String, JsonElement> compEntry : json.get(COMP_KEY).getAsJsonObject().entrySet()) {
            InlineStyle.InlineStyleComponent comp = InlineStyle.InlineStyleComponent.ALL_COMPS.get(compEntry.getKey());
            if (comp == null) continue;
            Optional<?> compVal = comp.codec().parse(JsonOps.INSTANCE, compEntry.getValue()).result();
            compVal.ifPresent(val -> copiedStyle.setComponent(comp, val));
        }
        return copiedStyle;
    }

    @SuppressWarnings("unchecked")
    //@ModifyReturnValue(method = "serialize", at = @At("RETURN"))
    private JsonElement HexPatStySerialize(JsonElement jsonElement, Style style, Type type, JsonSerializationContext jsonSerializationContext) {
        if (jsonElement == null || !jsonElement.isJsonObject()) {
            return jsonElement;
        }
        JsonObject json = jsonElement.getAsJsonObject();
        JsonObject compsJson = new JsonObject();
        // save all comps
        for (InlineStyle.InlineStyleComponent comp : style.getComponents()) {
            Optional<JsonElement> dataElem = comp.codec().encodeStart(JsonOps.INSTANCE, style.getComponent(comp)).result();
            dataElem.ifPresent(element -> compsJson.add(comp.id(), element));
        }
        if (!compsJson.isEmpty()) json.add(COMP_KEY, compsJson);
        return json;
    }*/
}
