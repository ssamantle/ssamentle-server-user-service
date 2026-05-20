package com.ssafy.ssamentle.error.exception;

import com.ssafy.ssamentle.error.GeneralException;
import com.ssafy.ssamentle.error.ResponseCode;

public class RedisHandler extends GeneralException {

    public RedisHandler(ResponseCode errorCode) {
        super(errorCode);
    }
}
