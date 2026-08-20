package com.study.chat.validation.annotation;

import com.study.chat.validation.validator.EmailDuplicateValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 이미 가입된 이메일인지 검증한다.
 */
@Documented
@Constraint(validatedBy = EmailDuplicateValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotDuplicateEmail {

    String message() default "USER_EMAIL_DUPLICATED";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
