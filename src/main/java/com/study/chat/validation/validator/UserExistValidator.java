package com.study.chat.validation.validator;

import com.study.chat.apiPayload.code.status.ErrorStatus;
import com.study.chat.service.user.UserQueryService;
import com.study.chat.validation.annotation.ExistUser;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Repository를 직접 주입하지 않고 QueryService를 거친다.
 * 영속성 계층에 접근하는 통로를 service 하나로 유지하기 위함이다.
 */
@Component
@RequiredArgsConstructor
public class UserExistValidator implements ConstraintValidator<ExistUser, Long> {

    private final UserQueryService userQueryService;

    @Override
    public void initialize(ExistUser constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Long value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // null 여부는 @NotNull의 책임
        }

        boolean isValid = userQueryService.existsById(value);

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(ErrorStatus.USER_NOT_FOUND.toString())
                    .addConstraintViolation();
        }

        return isValid;
    }
}
