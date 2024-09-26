package com.pyeondongbu.editorrecruitment.domain.community.service;

import com.pyeondongbu.editorrecruitment.domain.community.dao.CommunityPostRepository;
import com.pyeondongbu.editorrecruitment.domain.community.domain.CommunityPost;
import com.pyeondongbu.editorrecruitment.domain.community.dto.request.CommunityPostReq;
import com.pyeondongbu.editorrecruitment.domain.community.dto.response.CommunityPostRes;
import com.pyeondongbu.editorrecruitment.domain.member.dao.MemberRepository;
import com.pyeondongbu.editorrecruitment.domain.member.domain.Member;
import com.pyeondongbu.editorrecruitment.global.annotation.DistributedLock;
import com.pyeondongbu.editorrecruitment.global.exception.AuthException;
import com.pyeondongbu.editorrecruitment.global.exception.PostException;
import com.pyeondongbu.editorrecruitment.global.service.RedisLockService;
import com.pyeondongbu.editorrecruitment.global.validation.PostValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.pyeondongbu.editorrecruitment.global.exception.ErrorCode.INVALID_USER_NAME;
import static com.pyeondongbu.editorrecruitment.global.exception.ErrorCode.NOT_FOUND_POST_NAME;

@Service
@RequiredArgsConstructor
public class CommunityPostServiceImpl implements CommunityPostService {

    private final CommunityPostRepository postRepository;
    private final MemberRepository memberRepository;
    private final PostValidationUtils validationUtils;
    private final RedisLockService redisLockService;


    @Override
    @Transactional
    public CommunityPostRes create(final CommunityPostReq request, final Long memberId) {
        final Member member = memberRepository.findByIdWithDetails(memberId)
                .orElseThrow(() -> new AuthException(INVALID_USER_NAME));
        final CommunityPost post = CommunityPost.builder()
                .member(member)
                .build();

        return createOrUpdatePost(post, request);
    }

    @Override
    @Transactional
    public CommunityPostRes update(final Long postId, final CommunityPostReq request, final Long memberId) {
        final CommunityPost post = postRepository.findByMemberIdAndId(memberId, postId)
                .orElseThrow(() -> new PostException(NOT_FOUND_POST_NAME));
        return createOrUpdatePost(post, request);
    }

    @Override
    @Transactional(readOnly = true)
    @DistributedLock
    public CommunityPostRes getPost(final Long postId, final String remoteAddr, final Long memberId) {
        final CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new PostException(NOT_FOUND_POST_NAME));

        if (validationUtils.validatePostView(postId, remoteAddr)) {
            post.incrementViewCount();
            postRepository.save(post);
        }

        boolean isAuthor = post.getMember().getId().equals(memberId);


        return CommunityPostRes.from(post, isAuthor);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommunityPostRes> getMyPosts(final Long memberId) {
        final List<CommunityPost> posts = postRepository.findByMemberId(memberId);
        return posts.stream()
                .map(CommunityPostRes::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommunityPostRes> listPosts() {
        final List<CommunityPost> posts = postRepository.findAll();
        return posts.stream()
                .map(CommunityPostRes::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deletePost(final Long postId, final Long memberId) {
        final CommunityPost post = postRepository.findByMemberIdAndId(memberId, postId)
                .orElseThrow(() -> new PostException(NOT_FOUND_POST_NAME));
        postRepository.delete(post);
    }

    /**
     * Private 함수들
     */

    private CommunityPostRes createOrUpdatePost(
            final CommunityPost post,
            final CommunityPostReq req
    ) {
        post.update(req);
        postRepository.save(post);
        return CommunityPostRes.from(post);
    }

}
