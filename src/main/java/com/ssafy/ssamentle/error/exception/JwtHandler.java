package com.ssafy.ssamentle.error.exception;

import com.ssafy.ssamentle.error.GeneralException;
import com.ssafy.ssamentle.error.ResponseCode;

public class JwtHandler extends GeneralException {

    public JwtHandler(ResponseCode errorCode) {
        super(errorCode);
    }
}
