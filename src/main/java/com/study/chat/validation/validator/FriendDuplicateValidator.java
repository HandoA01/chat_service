package com.study.chat.validation.validator;

import com.study.chat.apiPayload.code.status.ErrorStatus;
import com.study.chat.config.security.CustomUserDetails;
import com.study.chat.service.friend.FriendQueryService;
import com.study.chat.validation.annotation.NotAlreadyFriend;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

        Long myId = currentUserId();
        if (myId == null) {
            // 인증이 없으면 검증할 기준이 없다. 인가 단계에서 401로 처리되므로 여기서는 통과시킨다.
            return true;
        }

        boolean isValid = !friendQueryService.isAlreadyRequested(myId, value);

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(ErrorStatus.FRIEND_ALREADY_REQUESTED.toString())
                    .addConstraintViolation();
        }

        return isValid;
    }

    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails principal) {
            return principal.getUserId();
        }
        return null;
    }
}
