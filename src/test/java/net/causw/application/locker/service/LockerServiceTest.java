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
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.util.MessageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static net.causw.application.locker.fixture.LockerTextFixture.*;
import static org.assertj.core.api.Assertions.*;
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
    @DisplayName("findById 성공 테스트 - locker가 존재할 때")
    public void findById_success() {
        // Given
        User expectedUser = UserFixture.createDefaultUser();
        Locker expectedLocker = LockerFixture.createAssignedLocker(expectedUser);


        when(userRepository.findById(expectedUser.getId())).thenReturn(Optional.of(expectedUser)); // Mock 객체 행동 정의
        when(lockerRepository.findByIdForRead(expectedLocker.getId())).thenReturn(Optional.of(expectedLocker));

        // When
        LockerResponseDto actualLockerResponseDto = lockerService.findById(expectedLocker.getId(), expectedUser.getId());

        // Then
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

    @Test
    @DisplayName("findById 에러 테스트 - user가 존재하지 않을 때")
    void findById_fail_WhenUserNotFound() {
        // Given
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> lockerService.findById(LOCKER_ID, USER_ID))
                // Then
                .isInstanceOf(BadRequestException.class)
                .satisfies(exception -> {
                    BadRequestException badRequestException = (BadRequestException) exception;
                    assertThat(badRequestException.getErrorCode()).isEqualTo(ErrorCode.ROW_DOES_NOT_EXIST);
                    assertThat(badRequestException.getMessage()).isEqualTo(MessageUtil.LOGIN_USER_NOT_FOUND);
                });

        verify(userRepository, times(1)).findById(USER_ID);
        verify(lockerRepository, never()).findByIdForRead(any());
    }

    @Test
    @DisplayName("findById 에러 테스트 - Locker가 존재하지 않을 때")
    void findById_fail_WhenLockerNotFound() {
        // Given
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(UserFixture.createDefaultUser()));
        when(lockerRepository.findById(LOCKER_ID)).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> lockerService.findById(LOCKER_ID, USER_ID))
        // Then
                .isInstanceOf(BadRequestException.class)
                .satisfies(exception -> {
                    BadRequestException badRequestException = (BadRequestException) exception;
                    assertThat(badRequestException.getErrorCode()).isEqualTo(ErrorCode.ROW_DOES_NOT_EXIST);
                    assertThat(badRequestException.getMessage()).isEqualTo(MessageUtil.LOCKER_NOT_FOUND);
                });

        verify(userRepository, times(1)).findById(USER_ID);
        verify(lockerRepository, times(1)).findByIdForRead(LOCKER_ID);
    }




}
