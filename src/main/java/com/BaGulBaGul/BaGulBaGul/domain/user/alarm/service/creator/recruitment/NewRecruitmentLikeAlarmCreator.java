package com.BaGulBaGul.BaGulBaGul.domain.user.alarm.service.creator.recruitment;

import com.BaGulBaGul.BaGulBaGul.domain.user.alarm.constant.AlarmType;
import com.BaGulBaGul.BaGulBaGul.domain.user.alarm.service.creator.AlarmCreator;
import com.BaGulBaGul.BaGulBaGul.domain.user.alarm.service.creator.event.NewEventLikeAlarmCreator;
import com.BaGulBaGul.BaGulBaGul.domain.user.alarm.service.creator.event.NewEventLikeAlarmCreator.Subject;
import com.BaGulBaGul.BaGulBaGul.domain.user.alarm.service.creator.post.NewPostLikeAlarmInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
public class NewRecruitmentLikeAlarmCreator extends AlarmCreator {

    @Builder
    public NewRecruitmentLikeAlarmCreator(
            Long recruitmentId,
            NewPostLikeAlarmInfo newPostLikeAlarmInfo
    ) {
        this.type = AlarmType.NEW_RECRUITMENT_LIKE;
        this.targetUserId = newPostLikeAlarmInfo.getTargetUserId();
        this.title = newPostLikeAlarmInfo.getTitle();
        this.message = newPostLikeAlarmInfo.getMessage();
        this.time = newPostLikeAlarmInfo.getTime();

        Subject subjectObject = Subject.builder()
                .recruitmentId(recruitmentId)
                .build();
        try {
            this.subject = makeSubjectJSON(subjectObject);
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException("AlarmCreator subject json 변환 실패");
        }
    }


    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    public static class Subject {
        Long recruitmentId;
    }
}
