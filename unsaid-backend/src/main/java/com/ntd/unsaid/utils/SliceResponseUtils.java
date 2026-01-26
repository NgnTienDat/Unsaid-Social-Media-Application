package com.ntd.unsaid.utils;

import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.function.Function;

public class SliceResponseUtils {

    public static <T, R> SliceResponse<R> build(Slice<T> slice, Function<T, R> mapper) {
        List<R> content = slice.getContent()
                .stream()
                .map(mapper)
                .toList();

        return SliceResponse.<R>builder()
                .content(content)
                .size(content.size())
                .hasNext(slice.hasNext())
                .build();
    }
}

