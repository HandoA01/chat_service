package com.study.chat.validation.validator;

import com.study.chat.apiPayload.code.status.ErrorStatus;
import com.study.chat.validation.annotation.CheckPage;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class PageValidator implements ConstraintValidator<CheckPage, Integer> {

    @Override
    public void initialize(CheckPage constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        boolean isValid = value != null && value >= 1;

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(ErrorStatus.MESSAGE_PAGE_INVALID.toString())
                    .addConstraintViolation();
        }

        return isValid;
    }
}
