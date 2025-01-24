package com.samsthenerd.inline;

import com.samsthenerd.inline.api.client.InlineClientAPI;
import com.samsthenerd.inline.api.client.renderers.InlineEntityRenderer;
import com.samsthenerd.inline.api.client.renderers.InlineItemRenderer;
import com.samsthenerd.inline.api.client.renderers.InlineSpriteRenderer;
import com.samsthenerd.inline.api.client.renderers.PlayerHeadRenderer;
import com.samsthenerd.inline.api.data.EntityInlineData;
import com.samsthenerd.inline.api.data.ItemInlineData;
import com.samsthenerd.inline.api.data.ModIconData;
import com.samsthenerd.inline.api.data.PlayerHeadData;
import com.samsthenerd.inline.api.matching.InlineMatch.DataMatch;
import com.samsthenerd.inline.api.matching.MatcherInfo;
import com.samsthenerd.inline.api.matching.RegexMatcher.Standard;
import com.samsthenerd.inline.impl.ProfileComponentUtil;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.HoverEvent.ItemStackContent;
import net.minecraft.text.Style;
import net.minecraft.util.Identifier;

import java.util.Optional;
import java.util.UUID;

public class InlineClient {
    public static void initClient() {
        // InlineAutoConfig.init();

        addDefaultRenderers();
        addDefaultMatchers();
    }

    private static void addDefaultRenderers() {
        InlineClientAPI.INSTANCE.addRenderer(InlineItemRenderer.INSTANCE);
        InlineClientAPI.INSTANCE.addRenderer(InlineEntityRenderer.INSTANCE);
        InlineClientAPI.INSTANCE.addRenderer(InlineSpriteRenderer.INSTANCE);
        InlineClientAPI.INSTANCE.addRenderer(PlayerHeadRenderer.INSTANCE);
    }

    private static void addDefaultMatchers() {
        Identifier itemMatcherID = Inline.id("item");
        InlineClientAPI.INSTANCE.addMatcher(new Standard(Standard.IDENTIFIER_REGEX_INSENSITIVE, itemMatcherID, (String itemId) -> {
            Identifier itemActualId = Identifier.of(itemId.toLowerCase());
            if (!Registries.ITEM.containsId(itemActualId)) return null;
            Item item = Registries.ITEM.get(itemActualId);
            ItemStack stack = new ItemStack(item);
            HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_ITEM, new ItemStackContent(stack));
            return new DataMatch(new ItemInlineData(stack), Style.EMPTY.withHoverEvent(he));
        }, MatcherInfo.fromId(itemMatcherID)));

        Identifier entityMatcherID = Inline.id("entity");
        InlineClientAPI.INSTANCE.addMatcher(new Standard(Standard.IDENTIFIER_REGEX_INSENSITIVE, entityMatcherID, (String entityTypeId) -> {
            Identifier entTypeActualId = Identifier.of(entityTypeId.toLowerCase());
            if (!Registries.ENTITY_TYPE.containsId(entTypeActualId)) return null;
            EntityType entType = Registries.ENTITY_TYPE.get(entTypeActualId);
            EntityInlineData entData = EntityInlineData.fromType(entType);
            return new DataMatch(entData, Style.EMPTY.withHoverEvent(entData.getEntityDisplayHoverEvent()));
        }, MatcherInfo.fromId(entityMatcherID)));

        /*
        Identifier linkMatcherId = new Identifier(Inline.MOD_ID, "link");
        InlineClientAPI.INSTANCE.addMatcher(new RegexMatcher.Simple("\\[(.*)\\]\\((.*)\\)", linkMatcherId, (MatchResult mr) ->{
            String text = mr.group(1);
            String link = mr.group(2);
            ClickEvent ce = new ClickEvent(ClickEvent.Action.OPEN_URL, link);
            HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(link));
            MutableText linkText = Text.literal(text + " 🔗");
            linkText.setStyle(Style.EMPTY.withClickEvent(ce).withHoverEvent(he).withUnderline(true).withColor(Formatting.BLUE));
            return new TextMatch(linkText);
        }, MatcherInfo.fromId(linkMatcherId)));
        */

        // InlineClientAPI.INSTANCE.addMatcher(new Identifier(Inline.MOD_ID, "bolditalic"), new RegexMatcher.Simple("(?<ast>\\*{1,3})\\b([^*]+)(\\k<ast>)", (MatchResult mr) ->{
        //     String text = mr.group(2);
        //     int astCount = mr.group(1).length();
        //     MutableText linkText = Text.literal(text);
        //     linkText.setStyle(Style.EMPTY.withBold(astCount >= 2).withItalic(astCount % 2 == 1));
        //     return new TextMatch(linkText);
        // }));

        Identifier modMatcherId = Inline.id("mod");
        InlineClientAPI.INSTANCE.addMatcher(new Standard("[0-9A-Za-z._-]+", modMatcherId, (String modId) -> {
            String modIdLowercase = modId.toLowerCase();
            Optional<ModContainer> maybeMod = Inline.getMod(modIdLowercase);
            if (maybeMod.isEmpty()) {
                return null;
            }
            return new DataMatch(new ModIconData(modIdLowercase), ModIconData.getTooltipStyle(modIdLowercase));
        }, MatcherInfo.fromId(modMatcherId)));

        Identifier faceMatcherId = Inline.id("face");
        InlineClientAPI.INSTANCE.addMatcher(new Standard("[a-zA-Z0-9_]{1,16}|[a-f0-9]{8}(?:-[a-f0-9]{4}){4}[a-f0-9]{8}", faceMatcherId, playerNameOrUUID -> {
            ProfileComponent profile;
            if (playerNameOrUUID.length() > 16) {
                profile = ProfileComponentUtil.from(UUID.fromString(playerNameOrUUID), null);
            } else {
                profile = ProfileComponentUtil.from(null, playerNameOrUUID);
            }
            PlayerHeadData headData = new PlayerHeadData(profile);
            return new DataMatch(headData, Style.EMPTY.withHoverEvent(headData.getEntityDisplayHoverEvent()));
        }, MatcherInfo.fromId(faceMatcherId)));
    }
}
