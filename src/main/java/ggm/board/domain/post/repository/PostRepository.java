package ggm.board.domain.post.repository;

import ggm.board.domain.auth.entity.CustomUserDetails;
import ggm.board.domain.post.dto.PostDTO;
import ggm.board.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("SELECT new ggm.board.domain.post.dto.PostDTO(" +
            "p.id, p.title, p.createdAt, p.updatedAt, pc.content, pa.id, pa.name, COUNT(r), null" +
            ") FROM Post p " +
            "LEFT JOIN p.postContent pc " +
            "LEFT JOIN p.postAuthor pa " +
            "LEFT JOIN p.replies r ON r.deleted = false " +
            "WHERE p.id = :id")
    PostDTO findByIdWithDetails(@Param("id") long id);

    @Query("SELECT new ggm.board.domain.post.dto.PostDTO(" +
            "p.id, p.title, pa.name, p.createdAt, COUNT(r)" +
            ") FROM Post p " +
            "LEFT JOIN p.replies r ON r.deleted = false " +
            "LEFT JOIN p.postAuthor pa " +
            "GROUP BY p.id, p.title, pa.name, p.createdAt " +
            "ORDER BY p.createdAt DESC")
    Page<PostDTO> findAllPostsOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT new ggm.board.domain.post.dto.PostDTO(" +
            "p.id, p.title, pa.name, p.createdAt, COUNT(r)" +
            ") FROM Post p " +
            "LEFT JOIN p.replies r ON r.deleted = false " +
            "LEFT JOIN p.postContent pc " +
            "LEFT JOIN p.postAuthor pa " +
            "WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(pc.content) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "GROUP BY p.id, p.title, pa.name, p.createdAt " +
            "ORDER BY p.createdAt DESC")
    Page<PostDTO> findPostsByKeyword(@Param("keyword") String keyword, Pageable pageable);

//    @Query("UPDATE ")
//    PostDTO updateById(PostDTO postDTO);

    Page<Post> findByPostAuthor(@Param("postAuthor") long postAuthor, PageRequest pageRequest);
//    Page<Post> findByKeyword(@Param("keyword") String keyword, PageRequest pageRequest);
}
