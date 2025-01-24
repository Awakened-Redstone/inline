// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.samsthenerd.inline.utils.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;


import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public record SetCodec<E>(Codec<E> elementCodec, int minSize, int maxSize) implements CollectionCodec<E, Set<E>> {

    @Override
    public <T> DecoderState<T> getDecoderState(DynamicOps<T> ops) {
        return new DecoderState<>(ops);
    }

    @Override
    public String toString() {
        return name() + "Codec[" + elementCodec + ']';
    }

    @Override
    public String name() {
        return "Set";
    }

    private class DecoderState<T> implements CollectionCodec.DecoderState<E, Set<E>, T> {
        private static final DataResult<Unit> INITIAL_RESULT = DataResult.success(Unit.INSTANCE, Lifecycle.stable());

        private final DynamicOps<T> ops;
        private final Set<E> elements = new HashSet<>();
        private final Stream.Builder<T> failed = Stream.builder();
        private DataResult<Unit> result = INITIAL_RESULT;
        private int totalCount;

        private DecoderState(final DynamicOps<T> ops) {
            this.ops = ops;
        }

        public void accept(final T value) {
            totalCount++;
            if (elements.size() >= maxSize) {
                failed.add(value);
                return;
            }
            final DataResult<Pair<E, T>> elementResult = elementCodec.decode(ops, value);
            elementResult.error().ifPresent(error -> failed.add(value));
            elementResult.resultOrPartial().ifPresent(pair -> elements.add(pair.getFirst()));
            result = result.apply2stable((result, element) -> result, elementResult);
        }

        public DataResult<Pair<Set<E>, T>> build() {
            if (elements.size() < minSize) {
                return createTooShortError(elements.size());
            }
            final T errors = ops.createList(failed.build());
            final Pair<Set<E>, T> pair = Pair.of(Set.copyOf(elements), errors);
            if (totalCount > maxSize) {
                result = createTooLongError(totalCount);
            }
            return result.map(ignored -> pair).setPartial(pair);
        }
    }
}
