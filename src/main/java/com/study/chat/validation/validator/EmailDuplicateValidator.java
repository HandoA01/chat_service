package com.study.chat.validation.validator;

import com.study.chat.apiPayload.code.status.ErrorStatus;
import com.study.chat.service.user.UserQueryService;
import com.study.chat.validation.annotation.NotDuplicateEmail;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailDuplicateValidator implements ConstraintValidator<NotDuplicateEmail, String> {

    private final UserQueryService userQueryService;

    @Override
    public void initialize(NotDuplicateEmail constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // 공백 여부는 @NotBlank의 책임
        }

        boolean isValid = !userQueryService.isEmailDuplicated(value);

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(ErrorStatus.USER_EMAIL_DUPLICATED.toString())
                    .addConstraintViolation();
        }

        return isValid;
    }
}
