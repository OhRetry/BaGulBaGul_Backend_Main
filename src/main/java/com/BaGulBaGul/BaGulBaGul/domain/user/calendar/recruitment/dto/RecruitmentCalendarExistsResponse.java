package com.BaGulBaGul.BaGulBaGul.domain.user.calendar.recruitment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecruitmentCalendarExistsResponse {
    boolean exists;
}