package net.causw.application.locker.fixture;

import net.causw.adapter.persistence.locker.Locker;
import net.causw.adapter.persistence.user.User;
import java.time.LocalDateTime;

public class LockerFixture {
    private static Locker.LockerBuilder baseLocker() {
        return Locker.builder()
                .lockerNumber(1L)
                .isActive(true)
                .location(LockerLocationFixture.createDefaultLocation())
                .expireDate(null);
    }


    public static Locker createAssignedLocker(User user) {
        return baseLocker()
                .isActive(false)
                .user(user)
                .location(LockerLocationFixture.createLocationWithName("Assigned Location"))
                .expireDate(LocalDateTime.now().plusMonths(1))
                .build();
    }

}