package ggm.board.domain.post.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReplyDTO {
    private long id;
    private String content;
    private long authorId;
    private String authorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long postId;
    private Long parentId;
    private List<ReplyDTO> children = new ArrayList<>();

    public ReplyDTO(long id, String content, long authorId, String authorName, LocalDateTime createdAt, LocalDateTime updatedAt, long postId, Long parentId) {
        this.id = id;
        this.content = content;
        this.authorId = authorId;
        this.authorName = authorName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.postId = postId;
        this.parentId = parentId;
    }

    public void printAll(String tab) {
        System.out.println(tab + this.id);
        System.out.println(tab + this.content);
        System.out.println(tab + this.authorName);
        System.out.println(tab + "children : {");
        if (this.children != null) {
            for (ReplyDTO child : this.children) {
                child.printAll(tab + "  ");
            }
        }
        System.out.println(tab + "}");
    }
}
