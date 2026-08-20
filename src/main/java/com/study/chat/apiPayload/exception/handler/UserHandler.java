package com.study.chat.apiPayload.exception.handler;

import com.study.chat.apiPayload.code.BaseErrorCode;
import com.study.chat.apiPayload.exception.GeneralException;

public class UserHandler extends GeneralException {

    public UserHandler(BaseErrorCode code) {
        super(code);
    }
}
