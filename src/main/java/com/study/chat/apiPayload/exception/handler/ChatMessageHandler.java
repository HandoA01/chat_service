package com.study.chat.apiPayload.exception.handler;

import com.study.chat.apiPayload.code.BaseErrorCode;
import com.study.chat.apiPayload.exception.GeneralException;

public class ChatMessageHandler extends GeneralException {

    public ChatMessageHandler(BaseErrorCode code) {
        super(code);
    }
}
