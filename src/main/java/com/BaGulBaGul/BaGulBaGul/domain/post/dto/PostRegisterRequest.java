package com.BaGulBaGul.BaGulBaGul.domain.post.dto;

import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class PostRegisterRequest {
    @NotBlank
    private String title;
    private String content;
    private List<String> tags;
    private List<String> images;
}
