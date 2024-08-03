package com.BaGulBaGul.BaGulBaGul.domain.post.applicationevent;

import com.BaGulBaGul.BaGulBaGul.global.applicationevent.BasicTimeApplicationEvent;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewPostCommentChildLikeApplicationEvent extends BasicTimeApplicationEvent {
    //좋아요를 받은 대댓글의 id
    private Long postCommentChildId;
    //좋아요 누른 유저의 id
    private Long userId;

    public NewPostCommentChildLikeApplicationEvent(Long postCommentChildId, Long userId) {
        this.postCommentChildId = postCommentChildId;
        this.userId = userId;
    }
}
