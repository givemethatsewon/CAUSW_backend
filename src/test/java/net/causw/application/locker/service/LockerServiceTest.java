package net.causw.application.locker.service;

import jakarta.validation.Validator;
import net.causw.adapter.persistence.locker.Locker;
import net.causw.adapter.persistence.locker.LockerLocation;
import net.causw.adapter.persistence.locker.LockerLog;
import net.causw.adapter.persistence.repository.*;
import net.causw.adapter.persistence.user.User;
import net.causw.application.common.CommonService;
import net.causw.application.dto.locker.LockerCreateRequestDto;
import net.causw.application.dto.locker.LockerResponseDto;
import net.causw.application.locker.LockerActionFactory;
import net.causw.application.locker.LockerService;
import net.causw.application.locker.fixture.LockerFixture;
import net.causw.application.locker.fixture.LockerLocationFixture;
import net.causw.application.locker.fixture.LockerTextFixture;
import net.causw.application.locker.fixture.UserFixture;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.enums.LockerLogAction;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.util.MessageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.format.DateTimeFormatter;
import java.util.Optional;


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
    @DisplayName("findById 성공 테스트")
    public void findById_success() {
        // Given
        User expectedUser = UserFixture.createDefaultUser();
        Locker expectedLocker = LockerFixture.createAssignedLocker(expectedUser);

        when(userRepository.findById(expectedUser.getId())).thenReturn(Optional.of(expectedUser));
        when(lockerRepository.findByIdForRead(expectedLocker.getId())).thenReturn(Optional.of(expectedLocker));

        // When
        LockerResponseDto actualLockerResponseDto = lockerService.findById(expectedLocker.getId(), expectedUser.getId());

        // Then
        // responseDto 내용 확인
        assertThat(actualLockerResponseDto).isNotNull();
        assertThat(actualLockerResponseDto.getId()).isEqualTo(expectedLocker.getId());
        assertThat(actualLockerResponseDto.getLockerNumber()).isEqualTo(String.valueOf(expectedLocker.getLockerNumber()));
        assertThat(actualLockerResponseDto.getIsActive()).isEqualTo(expectedLocker.getIsActive());
        assertThat(actualLockerResponseDto.getIsMine()).isTrue();
        assertThat(actualLockerResponseDto.getExpireAt()).isEqualTo(expectedLocker.getExpireDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")));


        // repository call 확인
        verify(userRepository, times(1)).findById(expectedUser.getId());
        verify(lockerRepository, times(1)).findByIdForRead(expectedLocker.getId());
    }


    @Test
    @DisplayName("findById 에러 테스트 - user가 존재하지 않을 때")
    void findById_fail_WhenUserNotFound() {
        // Given
        when(userRepository.findById(LockerTextFixture.USER_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> lockerService.findById(LockerTextFixture.LOCKER_ID, LockerTextFixture.USER_ID))
                .isInstanceOf(BadRequestException.class)
                .satisfies(exception -> {
                    BadRequestException badRequestException = (BadRequestException) exception;
                    assertThat(badRequestException.getErrorCode()).isEqualTo(ErrorCode.ROW_DOES_NOT_EXIST);
                    assertThat(badRequestException.getMessage()).isEqualTo(MessageUtil.LOGIN_USER_NOT_FOUND);
                });

        // repository call 확인
        verify(userRepository, times(1)).findById(LockerTextFixture.USER_ID);
        verify(lockerRepository, never()).findByIdForRead(any());
    }

    @Test
    @DisplayName("findById 에러 테스트 - Locker가 존재하지 않을 때")
    void findById_fail_WhenLockerNotFound() {
        // Given
        when(userRepository.findById(LockerTextFixture.USER_ID)).thenReturn(Optional.of(UserFixture.createDefaultUser()));
        when(lockerRepository.findById(LockerTextFixture.LOCKER_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> lockerService.findById(LockerTextFixture.LOCKER_ID, LockerTextFixture.USER_ID))
                .isInstanceOf(BadRequestException.class)
                .satisfies(exception -> {
                    BadRequestException badRequestException = (BadRequestException) exception;
                    assertThat(badRequestException.getErrorCode()).isEqualTo(ErrorCode.ROW_DOES_NOT_EXIST);
                    assertThat(badRequestException.getMessage()).isEqualTo(MessageUtil.LOCKER_NOT_FOUND);
                });

        // repository call 확인
        verify(userRepository, times(1)).findById(LockerTextFixture.USER_ID);
        verify(lockerRepository, times(1)).findByIdForRead(LockerTextFixture.LOCKER_ID);
    }

    @Test
    @DisplayName("create 성공 테스트")
    void create_success() {
        // Given
        LockerCreateRequestDto requestDto = new LockerCreateRequestDto(LockerTextFixture.LOCKER_NUMBER, LockerTextFixture.LOCKER_LOCATION_ID);

        User creator = UserFixture.createUserWithRole(Role.PRESIDENT);
        LockerLocation location = LockerLocationFixture.createDefaultLocation();
        Locker expectedLocker = LockerFixture.createDefaultLocker();

        when(userRepository.findById(LockerTextFixture.USER_ID)).thenReturn(Optional.of(creator));
        when(lockerLocationRepository.findById(LockerTextFixture.LOCKER_LOCATION_ID)).thenReturn(Optional.of(location));
        when(lockerRepository.findByLockerNumber(LockerTextFixture.LOCKER_NUMBER)).thenReturn(Optional.empty());
        when(lockerRepository.save(any(Locker.class))).thenReturn(expectedLocker);

        ArgumentCaptor<Locker> lockerCaptor = ArgumentCaptor.forClass(Locker.class);
        ArgumentCaptor<LockerLog> lockerLogCaptor = ArgumentCaptor.forClass(LockerLog.class);

        // When
        LockerResponseDto actualLockerResponseDto = lockerService.create(LockerTextFixture.USER_ID, requestDto);

        // Then
        // repository call 확인 - ArgumentCaptor 때문에 assert 전 먼저 확인
        verify(userRepository, times(1)).findById(LockerTextFixture.USER_ID);
        verify(lockerLocationRepository, times(1)).findById(LockerTextFixture.LOCKER_LOCATION_ID);
        verify(lockerRepository, times(1)).findByLockerNumber(LockerTextFixture.LOCKER_NUMBER);
        verify(lockerRepository, times(1)).save(lockerCaptor.capture());
        verify(lockerLogRepository, times(1)).save(lockerLogCaptor.capture());

        // LockerResponseDto 내용 확인
        assertThat(actualLockerResponseDto).isNotNull();
        assertThat(actualLockerResponseDto.getId()).isEqualTo(expectedLocker.getId());
        assertThat(actualLockerResponseDto.getLockerNumber()).isEqualTo(String.valueOf(LockerTextFixture.LOCKER_NUMBER));
        assertThat(actualLockerResponseDto.getIsActive()).isTrue();
        assertThat(actualLockerResponseDto.getIsMine()).isFalse(); // 이거 False 여야 하는지 확인 필요
        assertThat(actualLockerResponseDto.getExpireAt()).isNull();

        // 저장된 Locker 확인
        Locker capturedLocker = lockerCaptor.getValue();
        assertThat(capturedLocker.getLockerNumber()).isEqualTo(requestDto.getLockerNumber());
        assertThat(capturedLocker.getIsActive()).isTrue();
        assertThat(capturedLocker.getLocation()).isEqualTo(location);
        assertThat(capturedLocker.getUser()).isEqualTo(Optional.of(creator)); // 원래 근데 초기 create 이후에 사용자가 만든 사람으로 설정되나?? (locker 사용자 != locker 만든 사람)인데

        // 저장된 LockerLog 확인
        LockerLog capturedLockerLog = lockerLogCaptor.getValue();
        assertThat(capturedLockerLog.getLockerNumber()).isEqualTo(LockerTextFixture.LOCKER_NUMBER);
        assertThat(capturedLockerLog.getLockerLocationName()).isEqualTo(location.getName());
        assertThat(capturedLockerLog.getUserEmail()).isEqualTo(creator.getEmail());
        assertThat(capturedLockerLog.getUserName()).isEqualTo(creator.getName());
        assertThat(capturedLockerLog.getAction()).isEqualTo(LockerLogAction.ENABLE);
        assertThat(capturedLockerLog.getMessage()).isEqualTo(MessageUtil.LOCKER_FIRST_CREATED);

    }

    @Test
    @DisplayName("create 실패 테스트 - 사용자를 찾을 수 없는 경우")
    void create_fail_userNotFound() {
        // Given
        String creatorId = LockerTextFixture.INVALID_USER_ID;
        LockerCreateRequestDto requestDto = new LockerCreateRequestDto(LockerTextFixture.LOCKER_NUMBER, LockerTextFixture.LOCKER_LOCATION_NAME);

        when(userRepository.findById(creatorId)).thenReturn(Optional.empty()); // 오류 상황

        // When & Then
        // Error 내용 확인
        assertThatThrownBy(() -> lockerService.create(creatorId, requestDto))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ROW_DOES_NOT_EXIST)
                .hasMessage(MessageUtil.LOGIN_USER_NOT_FOUND);

        // repository call 확인
        verify(userRepository, times(1)).findById(LockerTextFixture.INVALID_USER_ID);
        verify(lockerLocationRepository, never()).findById(any());
        verify(lockerRepository, never()).findByLockerNumber(any());
        verify(lockerRepository, never()).save(any());
        verify(lockerLogRepository, never()).save(any());

    }

    @Test
    @DisplayName("create 실패 테스트 - Locker 위치를 찾을 수 없는 경우")
    void create_fail_locationNotFound() {
        // Given
        String creatorId = LockerTextFixture.USER_ID;
        LockerCreateRequestDto requestDto = new LockerCreateRequestDto(LockerTextFixture.LOCKER_NUMBER, LockerTextFixture.LOCKER_LOCATION_ID);
        User creator = UserFixture.createUserWithRole(Role.PRESIDENT);

        when(userRepository.findById(creatorId)).thenReturn(Optional.of(creator));
        when(lockerLocationRepository.findById(requestDto.getLockerLocationId())).thenReturn(Optional.empty()); // 오류 상황

        // When
        assertThatThrownBy(() -> lockerService.create(creatorId, requestDto))
                // Then
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ROW_DOES_NOT_EXIST)
                .hasMessage(MessageUtil.LOCKER_WRONG_POSITION);

        // repository call 확인
        verify(userRepository, times(1)).findById(LockerTextFixture.USER_ID);
        verify(lockerLocationRepository, times(1)).findById(LockerTextFixture.LOCKER_LOCATION_ID);
        verify(lockerRepository, never()).findByLockerNumber(any());
        verify(lockerRepository, never()).save(any());
        verify(lockerLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("create 실패 테스트 - 이미 존재하는 locker 번호")
    void create_fail_duplicateLockerNumber() {
        // Given
        String creatorId = LockerTextFixture.USER_ID;
        LockerCreateRequestDto requestDto = new LockerCreateRequestDto(LockerTextFixture.LOCKER_NUMBER, LockerTextFixture.LOCKER_LOCATION_ID);
        User creator = UserFixture.createUserWithRole(Role.PRESIDENT);
        LockerLocation location = LockerLocationFixture.createDefaultLocation();

        when(userRepository.findById(creatorId)).thenReturn(Optional.of(creator));
        when(lockerLocationRepository.findById(requestDto.getLockerLocationId())).thenReturn(Optional.of(location));
        when(lockerRepository.findByLockerNumber(requestDto.getLockerNumber())).thenReturn(Optional.of(LockerFixture.createDefaultLocker())); // LockerNumber로 쿼리한 Locker가 이미 존재

        // When & Then
        assertThatThrownBy(() -> lockerService.create(creatorId, requestDto))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ROW_ALREADY_EXIST)
                .hasMessage(MessageUtil.LOCKER_DUPLICATE_NUMBER);

        // repository call 확인
        verify(userRepository, times(1)).findById(LockerTextFixture.USER_ID);
        verify(lockerLocationRepository, times(1)).findById(LockerTextFixture.LOCKER_LOCATION_ID);
        verify(lockerRepository, times(1)).findByLockerNumber(LockerTextFixture.LOCKER_NUMBER);
        verify(lockerRepository, never()).save(any());
        verify(lockerLogRepository, never()).save(any());
    }

}





