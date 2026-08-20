package com.study.chat.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 휴대폰 기본 유니코드 이모지(😀)는 별도 개체가 아니라 문자이므로 여기 content에 저장된다.
 * (DB 문자셋 utf8mb4 필요) 구매하는 캐릭터 이모티콘은 EmojiMessage로 따로 간다.
 */
@Entity
@Table(name = "text_messages")
@DiscriminatorValue("TEXT")
@PrimaryKeyJoinColumn(name = "message_id")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TextMessage extends ChatMessage {

    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
}
