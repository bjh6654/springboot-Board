package ggm.board.domain.post.service;

import ggm.board.domain.auth.entity.CustomUserDetails;
import ggm.board.domain.member.repository.MemberUserRepository;
import ggm.board.domain.post.dto.PostDTO;
import ggm.board.domain.post.dto.ReplyDTO;
import ggm.board.domain.post.entity.Post;
import ggm.board.domain.post.entity.PostContent;
import ggm.board.domain.post.entity.Reply;
import ggm.board.domain.post.repository.PostContentRepository;
import ggm.board.domain.post.repository.PostRepository;
import ggm.board.domain.post.repository.ReplyRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.query.Param;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final ReplyRepository replyRepository;
    private final MemberUserRepository memberUserRepository;
    private final PostContentRepository postContentRepository;

    public boolean isAdminOrAuthor(CustomUserDetails customUserDetails, long authorId) {
        if (customUserDetails == null) throw new AuthenticationCredentialsNotFoundException("it must be work by not anonymous user.");
        return (customUserDetails.getAuthority().equals("ADMIN") || customUserDetails.getId() == authorId);
    }

    public boolean isAuthor(CustomUserDetails customUserDetails, long authorId) {
        if (customUserDetails == null) throw new AuthenticationCredentialsNotFoundException("it must be work by not anonymous user.");
        return customUserDetails.getId() == authorId;
    }

    public Page<PostDTO> findAllPosts(PageRequest pageRequest) {
        return postRepository.findAllPostsOrderByCreatedAtDesc(pageRequest);
    }

    public Page<Post> findByAuthorId(@Param("postAuthor") long postAuthor, PageRequest pageRequest) {
        return postRepository.findByPostAuthor(postAuthor, pageRequest);
    }

    public Page<PostDTO> findPostsByKeyword(@Param("keyword") String keyword, PageRequest pageRequest) {
        return postRepository.findPostsByKeyword(keyword, pageRequest);
    }

    public void saveReply(ReplyDTO replyDTO) {
        replyRepository.save(
                Reply.builder()
                        .content(replyDTO.getContent())
                        .parentReply(replyDTO.getParentId() != null ? replyRepository.getReferenceById(replyDTO.getParentId()) : null)
                        .replyAuthor(memberUserRepository.getReferenceById(replyDTO.getAuthorId()))
                        .replyPost(postRepository.getReferenceById(replyDTO.getPostId()))
                        .build()
        );
    }

    @Transactional
    public PostDTO savePost(PostDTO postDTO) {
        Post post = postRepository.save(Post.builder()
                        .title(postDTO.getTitle())
                        .postAuthor(memberUserRepository.getReferenceById(postDTO.getAuthorId()))
                        .build()
        );
        PostContent postContent = postContentRepository.save(PostContent.builder()
                        .content(postDTO.getContent())
                        .post(post)
                .build());
        PostDTO savedPostDTO = new PostDTO(post);
        savedPostDTO.setContent(postContent.getContent());
        return savedPostDTO;
    }

    public void deletePost(@Param("postId") long postId, CustomUserDetails customUserDetails) {
        Post post = postRepository.findById(postId).orElseThrow(EntityNotFoundException::new);
        if (isAdminOrAuthor(customUserDetails, postId)) {
            post.setDeleted(true);
            return;
        }
        throw new AuthorizationDeniedException("You are not allowed to delete this post");
    }

    @Transactional
    public void deleteReply(@Param("replyId") long replyId, CustomUserDetails customUserDetails) {
        Reply reply = replyRepository.findById(replyId).orElseThrow(EntityNotFoundException::new);
        if (isAdminOrAuthor(customUserDetails, replyId)) {
            reply.setDeleted(true);
            return;
        }
        throw new AuthorizationDeniedException("You are not allowed to delete this reply");
    }

    @Transactional
    public PostDTO updatePost(PostDTO postDTO, CustomUserDetails customUserDetails) {
        if (isAuthor(customUserDetails, postDTO.getAuthorId())) {
            Post post = postRepository.findById(postDTO.getId()).orElseThrow(() -> new EntityNotFoundException("Post not found"));
            post.changeContent(postDTO.getContent());
            post.setTitle(postDTO.getTitle());
            return new PostDTO(post);
        }
        throw new AuthorizationDeniedException("You are not allowed to update this post");
    }

    @Transactional
    public PostDTO findByIdWithDetails(long id) {
        postRepository.increaseView(id);
        PostDTO postDTO = postRepository.findByIdWithDetails(id).orElseThrow(EntityNotFoundException::new);
        postDTO.setReplies(replyRepository.findByPostIdOrderByCreatedAt(id));
        return postDTO;
    }
}
