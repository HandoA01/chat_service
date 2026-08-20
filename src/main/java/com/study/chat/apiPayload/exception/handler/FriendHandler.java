package com.study.chat.apiPayload.exception.handler;

import com.study.chat.apiPayload.code.BaseErrorCode;
import com.study.chat.apiPayload.exception.GeneralException;

public class FriendHandler extends GeneralException {

    public FriendHandler(BaseErrorCode code) {
        super(code);
    }
}
