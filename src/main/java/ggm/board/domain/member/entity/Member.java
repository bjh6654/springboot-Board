package ggm.board.domain.member.entity;

import ggm.board.domain.post.entity.Post;
import ggm.board.domain.post.entity.Reply;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor @AllArgsConstructor
@Table(name = "member")
public class Member {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @OneToOne(mappedBy = "profileMember", cascade = CascadeType.ALL)
    private MemberProfile memberProfile;

    @OneToMany(mappedBy = "replyAuthor")
    private List<Reply> replies = new ArrayList<>();

    @OneToMany(mappedBy = "postAuthor")
    private List<Post> posts = new ArrayList<>();
}

