package com.BaGulBaGul.BaGulBaGul.global.upload;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class S3TempResource {
    @Id
    @JoinColumn(name = "resource_id")
    @OneToOne(fetch = FetchType.LAZY)
    private Resource resource;

    @Column(name = "upload_time")
    private LocalDateTime uploadTime;
}
