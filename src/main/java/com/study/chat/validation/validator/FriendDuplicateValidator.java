package com.study.chat.validation.validator;

import com.study.chat.apiPayload.code.status.ErrorStatus;
import com.study.chat.common.TempAuth;
import com.study.chat.service.friend.FriendQueryService;
import com.study.chat.validation.annotation.NotAlreadyFriend;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FriendDuplicateValidator implements ConstraintValidator<NotAlreadyFriend, Long> {

    private final FriendQueryService friendQueryService;

    @Override
    public void initialize(NotAlreadyFriend constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Long value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        boolean isValid = !friendQueryService.isAlreadyRequested(TempAuth.CURRENT_USER_ID, value);

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(ErrorStatus.FRIEND_ALREADY_REQUESTED.toString())
                    .addConstraintViolation();
        }

        return isValid;
    }
}
