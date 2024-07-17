package net.causw.application.locker.service;

import jakarta.validation.Validator;
import net.causw.adapter.persistence.repository.*;
import net.causw.application.common.CommonService;
import net.causw.application.locker.LockerActionFactory;
import net.causw.application.locker.LockerService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

public class LockerServiceTest {
    private LockerRepository lockerRepository;  // 사물함 정보 처리
    private UserRepository userRepository; // 사용자 정보 처리 -> 권한 확인
    private LockerLogRepository lockerLogRepository; // 사물함 관련 로그를 추적
    private LockerLocationRepository lockerLocationRepository; // 사물함 위치 정보 처리
    private Validator validator; // 객체 유효성 검증
    private LockerActionFactory lockerActionFactory; // 사물함 관련 작업 처리
    private CommonService commonService; // 공통적으로 사용하는 서비스 로직
    private TextFieldRepository textFieldRepository;
    private FlagRepository flagRepository;
    private LockerService lockerService;

    @BeforeEach
    public void beforeEach() {
        // commonService init
        textFieldRepository = Mockito.mock(TextFieldRepository.class);
        flagRepository = Mockito.mock(FlagRepository.class);

        commonService = new CommonService(
                textFieldRepository,
                flagRepository
        );

        // lockerService init
        lockerRepository = Mockito.mock(LockerRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        lockerLogRepository = Mockito.mock(LockerLogRepository.class);
        lockerLocationRepository = Mockito.mock(LockerLocationRepository.class);
        validator = Mockito.mock(Validator.class);
        lockerActionFactory = Mockito.mock(LockerActionFactory.class);

        lockerService = new LockerService(
                lockerRepository,
                userRepository,
                lockerLogRepository,
                lockerLocationRepository,
                validator,
                lockerActionFactory,
                commonService
        );

    }

    @DisplayName("Test for Test")
    @Test
    public void testForTest() {
        Integer expectedLockerNumber = 1;
        Integer actualLockerNumber = 1;

        Assertions.assertThat(actualLockerNumber).isEqualTo(expectedLockerNumber);
    }
}
