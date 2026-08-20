package com.study.chat.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "emoji_messages")
@DiscriminatorValue("EMOJI")
@PrimaryKeyJoinColumn(name = "message_id")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmojiMessage extends ChatMessage {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emoticon_id", nullable = false)
    private Emoticon emoticon;
}
