package net.causw.application.locker.fixture;

import net.causw.adapter.persistence.locker.LockerLocation;

public class LockerLocationFixture {
    public static LockerLocation createDefaultLocation() {
        return LockerLocation.of("Default Location");
    }

    public static LockerLocation createLocationWithName(String name) {
        return LockerLocation.of(name);
    }
}