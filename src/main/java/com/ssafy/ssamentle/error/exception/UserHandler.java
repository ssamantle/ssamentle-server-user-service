package com.ssafy.ssamentle.error.exception;

import com.ssafy.ssamentle.error.ResponseCode;
import com.ssafy.ssamentle.error.GeneralException;

public class UserHandler extends GeneralException {

    public UserHandler(ResponseCode errorCode) { super(errorCode); }
}
