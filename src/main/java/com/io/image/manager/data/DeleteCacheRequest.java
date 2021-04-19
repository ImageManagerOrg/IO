package com.io.image.manager.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.util.List;


@Data
@NoArgsConstructor
public class DeleteCacheRequest {
    private List<Integer> keys;
    private List<Range> keyRanges;
}
