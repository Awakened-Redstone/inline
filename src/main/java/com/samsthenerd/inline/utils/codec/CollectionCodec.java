package com.samsthenerd.inline.utils.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;

public interface CollectionCodec<E, C extends Collection<E>> extends Codec<C> {
    default <R> DataResult<R> createTooShortError(final int size) {
        return DataResult.error(() -> name() + " is too short: " + size + ", expected range [" + minSize() + "-" + maxSize() + "]");
    }

    default <R> DataResult<R> createTooLongError(final int size) {
        return DataResult.error(() -> name() + " is too long: " + size + ", expected range [" + minSize() + "-" + maxSize() + "]");
    }

    @Override
    default <T> DataResult<T> encode(final C input, final DynamicOps<T> ops, final T prefix) {
        if (input.size() < minSize()) {
            return createTooShortError(input.size());
        }
        if (input.size() > maxSize()) {
            return createTooLongError(input.size());
        }
        final ListBuilder<T> builder = ops.listBuilder();
        for (final E element : input) {
            builder.add(elementCodec().encodeStart(ops, element));
        }
        return builder.build(prefix);
    }

    @Override
    default <T> DataResult<Pair<C, T>> decode(final DynamicOps<T> ops, final T input) {
        return ops.getList(input).setLifecycle(Lifecycle.stable()).flatMap(stream -> {
            final DecoderState<E, C, T> decoder = getDecoderState(ops);
            stream.accept(decoder::accept);
            return decoder.build();
        });
    }

    /*default String toString() {
        return name() + "Codec[" + elementCodec() + ']';
    }*/

    Codec<E> elementCodec();
    int minSize();
    int maxSize();
    String name();

    @ApiStatus.Internal
    <T> DecoderState<E, C, T> getDecoderState(final DynamicOps<T> ops);

    interface DecoderState<E, C extends Collection<E>, T> {
        void accept(final T value);
        DataResult<Pair<C, T>> build();
    }
}
