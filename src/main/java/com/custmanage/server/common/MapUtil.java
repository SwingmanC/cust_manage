package com.custmanage.server.common;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Map 工具类，兼容 Java 8（替代 Java 9+ 的 Map.of()）。
 * 用法：MapUtil.of("k1", v1, "k2", v2, ...)
 */
public final class MapUtil {

    private MapUtil() {}

    @SuppressWarnings("unchecked")
    public static Map<String, Object> of(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) {
            m.put((String) kv[i], kv[i + 1]);
        }
        return m;
    }
}
