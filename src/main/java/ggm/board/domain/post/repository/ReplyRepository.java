package ggm.board.domain.post.repository;

import ggm.board.domain.post.dto.ReplyDTO;
import ggm.board.domain.post.entity.Reply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;


public interface ReplyRepository extends JpaRepository<Reply, Long> {
    @Query("SELECT new ggm.board.domain.post.dto.ReplyDTO(" +
            "r.id, r.content, ra.id, ra.name, r.createdAt, r.updatedAt, rp.id, pr.id) FROM Reply r " +
            "JOIN r.replyAuthor ra " +
            "JOIN r.replyPost rp ON rp.id = :postId " +
            "LEFT JOIN r.parentReply pr " +
            "ORDER BY r.createdAt")
    List<ReplyDTO> findByPostIdOrderByCreatedAt(long postId);
}
