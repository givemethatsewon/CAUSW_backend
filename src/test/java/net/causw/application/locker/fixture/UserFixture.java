package net.causw.application.locker.fixture;

import net.causw.adapter.persistence.user.User;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.enums.UserState;
import org.springframework.test.util.ReflectionTestUtils;


public class UserFixture {

    private static User.UserBuilder baseUser() {
        return User.builder()
                .email("test@example.com")
                .name("Test User")
                .password("password123")
                .studentId("20230001")
                .admissionYear(2024)
                .role(Role.COMMON)
                .profileImage("default_profile.jpg")
                .state(UserState.ACTIVE)
                .refreshToken(null)
                .locker(null)
                .circleMemberList(null);
    }

    public static User createDefaultUser() {
        User user = baseUser()
                .build();
        ReflectionTestUtils.setField(user, "id" , LockerTextFixture.USER_ID); // LockerResponseDto의 isMine 검증을 위해 필요

        return user;
    }

}