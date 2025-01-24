package com.samsthenerd.inline.utils.codec;

import com.mojang.serialization.Codec;

import java.util.Collection;
import java.util.Set;

public class CodecUtils {
    public static <E, C extends Collection<E>> Codec<C> collectionOf(final Codec<E> elementCodec, final int minSize, final int maxSize, CollectionCodecProvider<E, C> provider) {
        return provider.construct(elementCodec, minSize, maxSize);
    }

    public static <E> Codec<Set<E>> setOf(final Codec<E> elementCodec) {
        return setOf(elementCodec, 0, Integer.MAX_VALUE);
    }

    public static <E> Codec<Set<E>> setOf(final Codec<E> elementCodec, final int minSize, final int maxSize) {
        // IntelliJ thinks I don't need to specify the types but the compiler says otherwise
        return CodecUtils.<E, Set<E>>collectionOf(elementCodec, minSize, maxSize, SetCodec::new);
    }

    @FunctionalInterface
    public interface CollectionCodecProvider<E, C extends Collection<E>> {
        Codec<C> construct(final Codec<E> elementCodec, final int minSize, final int maxSize);
    }
}
