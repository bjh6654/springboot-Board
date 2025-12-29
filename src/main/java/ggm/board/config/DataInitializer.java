package ggm.board.config;

import com.github.javafaker.Faker;
import ggm.board.domain.auth.entity.Auth;
import ggm.board.domain.auth.entity.UserRole;
import ggm.board.domain.auth.repository.AuthRepository;
import ggm.board.domain.member.entity.Member;
import ggm.board.domain.member.repository.MemberUserRepository;
import ggm.board.domain.post.entity.Post;
import ggm.board.domain.post.entity.PostContent;
import ggm.board.domain.post.entity.Reply;
import ggm.board.domain.post.repository.PostContentRepository;
import ggm.board.domain.post.repository.PostRepository;
import ggm.board.domain.post.repository.ReplyRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Configuration
public class DataInitializer {
    private final Faker faker = new Faker(new Locale("ko"));
    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    public void createReply(int num, Reply parent, Post post, List<Member> memberUsers, ReplyRepository replyRepository) {
        // 댓글 생성
        for (int r = 0; r < num; r++) {
            Reply child = Reply.builder()
                    .content(faker.lorem().sentence())
                    .replyAuthor(memberUsers.get(new Random().nextInt(memberUsers.size())))
                    .replyPost(post)
                    .parentReply(parent)
                    .build();
            replyRepository.save(child);

            int random = new Random().nextInt(Integer.min(num, 3));
            createReply(random, child, post, memberUsers, replyRepository);
        }
    }

    @Bean
    public CommandLineRunner initializeData(
            MemberUserRepository memberUserRepository,
            AuthRepository authRepository,
            PostRepository postRepository,
            PostContentRepository postContentRepository,
            ReplyRepository replyRepository) {

        return args -> {
            // 사용자 생성
            List<Member> memberUsers = new ArrayList<>();
            List<Auth> auths = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                Member user = Member.builder()
                        .name(faker.name().username())
                        .build();
                memberUsers.add(memberUserRepository.save(user));

                Auth auth = Auth.builder()
                        .authMember(user)
                        .email(faker.internet().emailAddress())
                        .password(bCryptPasswordEncoder.encode(faker.internet().password()))
                        .role(UserRole.USER)
                        .build();
                auths.add(authRepository.save(auth));
//                MemberAuth auth = MemberAuth.builder()
//                        .email(faker.internet().emailAddress())
//                        .type("naver")
//                        .authMember(user)
//                        .build();
//                memberAuths.add(memberAuthRepository.save(auth));
            }
            Member user = Member.builder()
                    .name("박진혁")
                    .build();
            memberUsers.add(memberUserRepository.save(user));

            auths.add(authRepository.save(Auth.builder()
                    .authMember(user)
                    .email("admin1065@naver.com")
                    .password(bCryptPasswordEncoder.encode("admin123!@#"))
                    .role(UserRole.USER)
                    .build())
            );

            // 게시글 생성
            List<Post> posts = new ArrayList<>();
            List<PostContent> postContents = new ArrayList<>();
            Date from = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(365 * 2)); // 약 2년 전
            Date to = new Date(System.currentTimeMillis());
            for (int i = 0; i < 80; i++) {
                Post post = Post.builder()
                        .title(faker.book().title())
                        .postAuthor(memberUsers.get(new Random().nextInt(memberUsers.size())))
//                        .replyCount(new Random().nextLong(1000))
//                        .createdAt(faker.date().between(from, to).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                        .build();
                Post savedPost = postRepository.save(post);
                posts.add(savedPost);

                PostContent postcontent = PostContent.builder()
//                        .id(i)
                        .post(savedPost)
                        .content(faker.lorem().paragraph())
                        .build();
                postContents.add(postContentRepository.save(postcontent));
            }

            Post savedPost = postRepository.save(Post.builder()
                    .title(faker.book().title())
                    .postAuthor(user)
//                        .replyCount(new Random().nextLong(1000))
//                        .createdAt(faker.date().between(from, to).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                    .build());
            posts.add(savedPost);

            PostContent postcontent = PostContent.builder()
//                        .id(i)
                    .post(savedPost)
                    .content(faker.lorem().paragraph())
                    .build();
            postContents.add(postContentRepository.save(postcontent));

            // 댓글 작성 대댓 포함
            for (int i = 0; i < 200; i++) {
                Post post = posts.get(new Random().nextInt(posts.size()));
                Reply reply = Reply.builder()
                        .content(faker.lorem().sentence())
                        .replyAuthor(memberUsers.get(new Random().nextInt(memberUsers.size())))
                        .replyPost(post)
                        .parentReply(null)
                        .build();
                replyRepository.save(reply);

                this.createReply(new Random().nextInt(4), reply, post, memberUsers, replyRepository);
            }


            System.out.println("✓ 더미 데이터 생성 완료");
        };
    }
}