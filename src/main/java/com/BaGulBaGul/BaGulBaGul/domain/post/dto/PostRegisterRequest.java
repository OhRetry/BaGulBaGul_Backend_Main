package com.BaGulBaGul.BaGulBaGul.domain.post.dto;

import java.util.List;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class PostRegisterRequest {
    private String title;
    private String content;
    private List<String> tags;
    private List<Long> imageIds;
}
