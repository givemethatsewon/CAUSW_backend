package net.causw.application.dto.form;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import net.causw.domain.model.enums.form.RegisteredSemester;
import net.causw.domain.model.enums.userAcademicRecord.AcademicStatus;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Getter
public class FormCreateRequestDto {

    @Schema(description = "신청서 제목", example = "신청서 제목입니다.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String title;

    @NotEmpty(message = "질문을 입력해 주세요.")
    @Schema(description = "질문", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<QuestionCreateRequestDto> questions;

    @NotNull(message = "재학생 답변 허용 여부를 선택해 주세요.")
    @Schema(description = "재학생 답변 허용 여부", example = "ture", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isAllowedEnrolled;

    @Schema(description = "재학생 답변 허용 시, 답변 가능한 등록 완료 학기(상관없음 시 1차부터 9차 이상까지 넣어서 요청)",
            example = "[FIRST_SEMESTER,SECOND_SEMESTER ... EIGHTH_SEMESTER,ABOVE_NIGHT_SEMESTER]", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<RegisteredSemester> enrolledRegisteredSemesterList;

    @Schema(description = "재학생 답변 허용 시, 학생회비 납부 필요 여부", example = "ture", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean isNeedCouncilFeePaid;

    @NotNull(message = "휴학생 답변 허용 여부를 선택해 주세요.")
    @Schema(description = "휴학생 답변 허용 여부", example = "ture", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isAllowedLeaveOfAbsence;

    @Schema(description = "휴학생 답변 허용 시, 답변 가능한 등록 완료 학기(상관없음 시 1차부터 9차 이상까지 넣어서 요청)",
            example = "[FIRST_SEMESTER,SECOND_SEMESTER ... EIGHTH_SEMESTER,ABOVE_NIGHT_SEMESTER]", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<RegisteredSemester> leaveOfAbsenceRegisteredSemesterList;

    @Schema(description = "졸업생 답변 허용 여부", example = "ture", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isAllowedGraduation;

}
