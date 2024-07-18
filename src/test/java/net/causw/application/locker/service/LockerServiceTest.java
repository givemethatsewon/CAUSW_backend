package net.causw.application.locker.service;

import jakarta.validation.Validator;
import net.causw.adapter.persistence.locker.Locker;
import net.causw.adapter.persistence.repository.*;
import net.causw.adapter.persistence.user.User;
import net.causw.application.common.CommonService;
import net.causw.application.dto.locker.LockerResponseDto;
import net.causw.application.locker.LockerActionFactory;
import net.causw.application.locker.LockerService;
import net.causw.application.locker.fixture.LockerFixture;
import net.causw.application.locker.fixture.UserFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

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
    public void init() {
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

    @Test
    @DisplayName("findById 테스트 - locker 존재하면 LockerResponseDto 리턴")
    public void findByIdTest() {
        // given
        User expectedUser = UserFixture.createDefaultUser();
        Locker expectedLocker = LockerFixture.createAssignedLocker(expectedUser);


        when(userRepository.findById(expectedUser.getId())).thenReturn(Optional.of(expectedUser)); // Mock 객체 행동 정의
        when(lockerRepository.findByIdForRead(expectedLocker.getId())).thenReturn(Optional.of(expectedLocker));

        // when
        LockerResponseDto actualLockerResponseDto = lockerService.findById(expectedLocker.getId(), expectedUser.getId());

        // then
        ArgumentCaptor<String> userIdCaptor = ArgumentCaptor.forClass(String.class); // parameterized test
        ArgumentCaptor<String> lockerIdCaptor = ArgumentCaptor.forClass(String.class);

        verify(userRepository, times(1)).findById(userIdCaptor.capture()); // 함수 1번 실행 됐는지 확인 및 repository 메서드 호출 시 전달된 인자 캡처
        verify(lockerRepository, times(1)).findByIdForRead(lockerIdCaptor.capture());

        assertThat(userIdCaptor.getValue()).isEqualTo(expectedUser.getId()); // 올바른 id가 repository 메소드에 전달되었는지 확인
        assertThat(lockerIdCaptor.getValue()).isEqualTo(expectedLocker.getId());

        assertThat(actualLockerResponseDto).isNotNull(); // lockerResponseDto가 잘 설정됐는지 확인
        assertThat(actualLockerResponseDto.getLockerNumber()).isEqualTo(String.valueOf(expectedLocker.getLockerNumber()));
        assertThat(actualLockerResponseDto.getIsActive()).isEqualTo(expectedLocker.getIsActive());
        assertThat(actualLockerResponseDto.getIsMine()).isTrue();
        assertThat(actualLockerResponseDto.getExpireAt()).isEqualTo(expectedLocker.getExpireDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")));
    }



    @DisplayName("Test for Test")
    @Test
    public void testForTest() {
        Integer expectedLockerNumber = 1;
        Integer actualLockerNumber = 1;

        assertThat(actualLockerNumber).isEqualTo(expectedLockerNumber);
    }



}
