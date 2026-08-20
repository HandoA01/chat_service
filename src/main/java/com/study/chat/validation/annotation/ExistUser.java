package com.study.chat.validation.annotation;

import com.study.chat.validation.validator.UserExistValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 대상 회원이 실제로 존재하는지 검증한다.
 * message에는 ErrorStatus의 enum 이름을 담아 ExceptionAdvice가 valueOf로 복원할 수 있게 한다.
 */
@Documented
@Constraint(validatedBy = UserExistValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExistUser {

    String message() default "USER_NOT_FOUND";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
