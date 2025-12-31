package ggm.board.domain.post.dto;

import ggm.board.domain.post.entity.Post;
import ggm.board.domain.post.entity.Reply;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor @NoArgsConstructor
public class PostDTO {
    private long id;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String content;
    private long authorId;
    private String authorName;
    private long replyCount;
    private List<ReplyDTO> replies;
    private long viewCount;

    // To make PostList at Board Page
    public PostDTO(long id, String title, String authorName, LocalDateTime createdAt, long replyCount, long viewCount) {
        this.id = id;
        this.title = title;
        this.authorName = authorName;
        this.createdAt = createdAt;
        this.replyCount = replyCount;
        this.viewCount = viewCount;
    }

    public PostDTO(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();
        if (post.getPostContent() != null) {
            this.content = post.getPostContent().getContent();
        }
        this.authorId = post.getPostAuthor().getId();
        this.authorName = post.getPostAuthor().getName();
    }
}
