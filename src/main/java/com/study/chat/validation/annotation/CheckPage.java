package com.study.chat.validation.annotation;

import com.study.chat.validation.validator.PageValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 클라이언트는 1부터 시작하는 페이지 번호를 보내고, 내부에서 0-based로 변환한다.
 * @Valid를 거치지 않는 @RequestParam 검증이라 ConstraintViolationException으로 바로 전달된다.
 */
@Documented
@Constraint(validatedBy = PageValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckPage {

    String message() default "MESSAGE_PAGE_INVALID";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
