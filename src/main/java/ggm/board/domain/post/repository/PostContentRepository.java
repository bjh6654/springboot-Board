package ggm.board.domain.post.repository;

import ggm.board.domain.post.entity.PostContent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostContentRepository extends JpaRepository<PostContent, Long> {

}
