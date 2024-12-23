package com.BaGulBaGul.BaGulBaGul.domain.recruitment.dto;

import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecruitmentConditionalApiRequest {

    @ApiModelProperty(value = "모집글이 속한 이벤트의 id")
    private Long eventId;

    @ApiModelProperty(value = "게시글 제목")
    private String title;

    @ApiModelProperty(value = "태그들", example = "[\"물놀이\",\"바베큐\"]")
    private List<String> tags;

    @ApiModelProperty(value = "등록자 닉네임")
    private String username;

    @ApiModelProperty(value = "남은 인원 수")
    private Integer leftHeadCount;

    public RecruitmentConditionalRequest toRecruitmentConditionalRequest() {
        return RecruitmentConditionalRequest.builder()
                .eventId(eventId)
                .title(title)
                .tags(tags)
                .username(username)
                .leftHeadCount(leftHeadCount)
                .build();
    }
}
