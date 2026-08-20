package com.study.chat.validation.annotation;

import com.study.chat.validation.validator.FriendDuplicateValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 이미 친구 요청을 보냈거나 친구인 상대인지 검증한다.
 */
@Documented
@Constraint(validatedBy = FriendDuplicateValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotAlreadyFriend {

    String message() default "FRIEND_ALREADY_REQUESTED";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
