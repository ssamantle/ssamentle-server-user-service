package com.ssafy.ssamentle.util;

import org.springframework.stereotype.Component;

@Component
public class RedisKeyNamingUtil {

    private static final String RT_SESSION_PREFIX = "rt:session";

    public static String REFRESH_TOKEN_REDIS_KEY_NAME(String familyId) {

        return RT_SESSION_PREFIX + familyId;
    }
}
