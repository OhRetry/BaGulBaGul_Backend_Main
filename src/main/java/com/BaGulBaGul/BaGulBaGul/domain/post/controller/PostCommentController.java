package com.BaGulBaGul.BaGulBaGul.domain.post.controller;


import com.BaGulBaGul.BaGulBaGul.domain.post.dto.*;
import com.BaGulBaGul.BaGulBaGul.global.response.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostCommentController {
    ApiResponse<PostCommentDetailResponse> getPostCommentDetail(Long postCommentId);
    ApiResponse<Page<GetPostCommentPageResponse>> getPostCommentPage(Long postId, Long requestUserId, Pageable pageable);
    ApiResponse<Page<GetPostCommentChildPageResponse>> getPostCommentChildPage(Long postCommentId, Long requestUserId, Pageable pageable);
    ApiResponse<PostCommentRegisterResponse> registerPostComment(Long postId, Long userId, PostCommentRegisterRequest postCommentRegisterRequest);
    ApiResponse<Object> modifyPostComment(Long postCommentId, Long userId, PostCommentModifyRequest postCommentModifyRequest);
    ApiResponse<Object> deletePostComment(Long postCommentId, Long userId);
    ApiResponse<PostCommentChildRegisterResponse> registerPostCommentChild(Long postCommentId, Long userId, PostCommentChildRegisterRequest postCommentChildRegisterRequest);
    ApiResponse<Object> modifyPostCommentChild(Long postCommentChildId, Long userId, PostCommentChildModifyRequest postCommentChildModifyRequest);
    ApiResponse<Object> deletePostCommentChild(Long postCommentChildId, Long userId);
    ApiResponse<LikeCountResponse> addLikeToComment(Long postCommentId, Long userId);
    ApiResponse<LikeCountResponse> deleteLikeToComment(Long postCommentId, Long userId);
    ApiResponse<IsMyLikeResponse> isMyLikeComment(Long postCommentId, Long userId);
    ApiResponse<LikeCountResponse> addLikeToCommentChild(Long postCommentChildId, Long userId);
    ApiResponse<LikeCountResponse> deleteLikeToCommentChild(Long postCommentChildId, Long userId);
    ApiResponse<IsMyLikeResponse> isMyLikeCommentChild(Long postCommentChildId, Long userId);
}
