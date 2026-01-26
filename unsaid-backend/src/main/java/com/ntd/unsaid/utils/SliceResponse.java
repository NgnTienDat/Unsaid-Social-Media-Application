package com.ntd.unsaid.utils;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SliceResponse<T> {
    List<T> content;
    int size;
    boolean hasNext;
}
