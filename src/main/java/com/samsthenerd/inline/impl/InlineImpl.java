package com.samsthenerd.inline.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.samsthenerd.inline.api.InlineAPI;
import com.samsthenerd.inline.api.InlineData;
import com.samsthenerd.inline.api.InlineData.InlineDataType;
import com.samsthenerd.inline.api.matching.InlineMatcher;
import net.minecraft.text.Style;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class InlineImpl implements InlineAPI {

    private static final Map<Identifier, InlineDataType<?>> DATA_TYPES = new HashMap<>();

    @Override
    public void addDataType(InlineDataType<?> type){
        DATA_TYPES.put(type.getId(), type);
    }

    private static final Codec<InlineDataType<?>> INLINE_DATA_TYPE_CODEC = Identifier.CODEC.comapFlatMap(
            id -> DATA_TYPES.containsKey(id)
                    ? DataResult.success(DATA_TYPES.get(id))
                    : DataResult.error(() -> "No inline data type: " + id.toString()),
            InlineDataType::getId);

    public static final Codec<InlineData<?>> INLINE_DATA_CODEC = INLINE_DATA_TYPE_CODEC.dispatch("type",
            InlineData::getType, (inlineDataType -> inlineDataType.getCodec().fieldOf("data")));

    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <D extends InlineData<D>> D deserializeData(JsonObject json){
        String type = json.get("type").getAsString();
        if(!DATA_TYPES.containsKey(Identifier.of(type))){
            return null;
        }
        InlineDataType<D> dType = (InlineDataType<D>)DATA_TYPES.get(Identifier.of(type));
        return dType.getCodec().parse(JsonOps.INSTANCE, json.get("data")).getOrThrow();
    }

    @Override
    public <D extends InlineData<D>> JsonObject serializeData(D data){
        InlineDataType<D> dType = data.getType();
        JsonObject json = new JsonObject();
        Optional<JsonElement> dataElem = dType.getCodec().encodeStart(JsonOps.INSTANCE, data).result();
        json.addProperty("type", data.getType().toString());
        json.add("data", dataElem.orElse(new JsonObject()));
        return json;
    }

    @Override
    public Style withSizeModifier(Style style, double modifier){
        return style.withComponent(InlineStyle.SIZE_MODIFIER_COMP, modifier);
    }

    public static final Set<InlineMatcher> SERVER_CHAT_MATCHERS = new HashSet<>();

    public void addChatMatcher(InlineMatcher matcher){
        SERVER_CHAT_MATCHERS.add(matcher);
    }
}
