package com.study.chat.apiPayload.exception.handler;

import com.study.chat.apiPayload.code.BaseErrorCode;
import com.study.chat.apiPayload.exception.GeneralException;

public class ChatRoomHandler extends GeneralException {

    public ChatRoomHandler(BaseErrorCode code) {
        super(code);
    }
}
