package ggm.board.domain.post.entity;

import ggm.board.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Formula;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder
@AllArgsConstructor @NoArgsConstructor
@Table(name = "post")
@EntityListeners(AuditingEntityListener.class)
public class Post {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column
    private String title;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Member postAuthor;

    @OneToMany(mappedBy = "replyPost", cascade = CascadeType.ALL)
    private List<Reply> replies = new ArrayList<>();

    @Formula("SELECT COUNT(*) FROM reply r WHERE r.post_id = id")
    private Long replyCount;

    @OneToOne(mappedBy = "post", cascade = CascadeType.ALL)
    private PostContent postContent;

    public void changeContent(String newContent) {
        this.postContent.setContent(newContent);
        this.updatedAt = LocalDateTime.now();
    }
}
